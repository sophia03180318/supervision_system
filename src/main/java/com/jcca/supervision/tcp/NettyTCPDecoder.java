package com.jcca.supervision.tcp;

import com.jcca.common.LogUtil;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.TcpResponseHandler;
import com.jcca.util.SpringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 增强版消息解码器，支持不完整报文（半包）和粘包处理
 *
 * @author sophia
 * @date   2023/11/23 9:08
 */
public class NettyTCPDecoder extends ByteToMessageDecoder {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            String hexDump = ByteBufUtil.hexDump(in);
            // 丢弃过短或心跳消息
            if (hexDump.length() < 24) {
                logger.warn(LogUtil.buildLog("丢弃过短的消息", hexDump));
                in.clear();
                return;
            }
            if (hexDump.substring(20).startsWith(DataConst.MSG_HEART_BEAT)) {
                logger.info(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "收到TCP心跳", "砰"));
                in.clear();
                return;
            }
            logger.info(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "收到TCP原始内容", hexDump));
            decodeFrames(in, out);
        } catch (Exception e) {
            logger.warn(LogUtil.buildLog("接收消息报错", e.toString()));
        }
    }

    /**
     * 处理ByteBuf中的所有完整帧，支持半包粘包
     */
    private void decodeFrames(ByteBuf in, List<Object> out) {
        while (true) {
            // 1. 确保至少能读到长度字段（4字节）
            if (in.readableBytes() < Integer.BYTES) {
                return;
            }
            in.markReaderIndex();
            long totalLen = in.readUnsignedInt(); // 总报文长度
            // 2. 如果可读字节不足一个完整包，重置读指针，等待更多数据
            if (in.readableBytes() < totalLen - 4) {
                in.resetReaderIndex();
                return;
            }
            // 3. 读取报文序号和类型
            long serialNo = in.readUnsignedInt();
            int type;
            try {
                type = (int) in.readUnsignedInt();
            } catch (NumberFormatException e) {
                logger.error("报文类型出错，跳过此帧");
                // 跳过剩余字节
                in.skipBytes((int) (totalLen - 12));
                continue;
            }
            // 4. 读取剩余内容到一个单独ByteBuf，以便解码器专注于本帧
            String hexDump = ByteBufUtil.hexDump(in);
            int bodyLen = (int) (totalLen - 12);
            ByteBuf bodyBuf = in.readRetainedSlice(bodyLen);
            try {
                TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
                Object decoded = handler.decode(type, bodyBuf);
                if (Objects.nonNull(decoded) && decoded instanceof DataBaseInfo) {
                    DataBaseInfo info = (DataBaseInfo) decoded;
                    info.setCode(type);
                    info.setNum(serialNo);
                    info.setTime(new Date());
                    out.add(info);
                }
            } catch (Exception e) {
                logger.error("解析报文失败: type={}, error={} ", type, e.toString());
            } finally {
                bodyBuf.release(); // 释放slice引用
            }
            // 循环处理下一个可能的帧
        }
    }
}
