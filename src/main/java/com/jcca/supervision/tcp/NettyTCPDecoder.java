package com.jcca.supervision.tcp;

import com.jcca.common.LogUtil;
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
 * @date 2023/11/23 9:08
 */
public class NettyTCPDecoder extends ByteToMessageDecoder {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            // 半包数据直接等待更多字节，不要清空缓存以免丢包
            if (in.readableBytes() < 12) {
                logger.info(LogUtil.buildLog("收到未完整的消息，等待更多数据", ByteBufUtil.hexDump(in)));
                return;
            }
            logger.info(LogUtil.buildLog("原始信息长度: ", in.readableBytes()));
            decodeFrames(ctx, in, out);
        } catch (Exception e) {
            logger.warn(LogUtil.buildLog("接收消息报错", e.toString()));
        }
    }

    /**
     * 处理ByteBuf中的所有完整帧，支持半包粘包
     */
    private void decodeFrames(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (true) {
            // 1. 标记读指针，开始尝试读取一个完整包
            in.markReaderIndex();

            // 2. 再次检查头部长度（循环中可能读空了）
            if (in.readableBytes() < 12) {
                in.resetReaderIndex();
                return;
            }
            long totalLen = in.readUnsignedInt(); // 总报文长度

            if (totalLen < 12) {
                logger.error("非法报文长度: " + totalLen + "，关闭连接");
                ctx.close();
                return;
            }

            // 3. 如果可读字节不足一个完整包，重置读指针，等待更多数据
            if (in.readableBytes() < totalLen - 4) {
                in.resetReaderIndex(); // 回退指针，等待下次数据到齐
                return;
            }
            // 4. 读取报文序号和类型
            long serialNo = in.readUnsignedInt();
            int type = (int) in.readUnsignedInt();

            //5.跳心跳帧
            if (type == 1201 || type == 1202) {
                logger.debug(LogUtil.buildLog("", "心跳"));
                // 剩余的 Info 部分长度 = totalLen -（length + serial + type）
                int skipLen = (int) (totalLen - 12);
                if (skipLen > 0) {
                    in.skipBytes(skipLen);
                }
                continue;
            }else{
                logger.info(LogUtil.buildLog("打印包内容", ByteBufUtil.hexDump(in)));
            }
            // 6. 业务报文处理
            int bodyLen = (int) (totalLen - 12);
            //使用 readRetainedSlice 增加引用计数，以匹配 finally 中的 release
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
                // 必须释放，否则内存泄漏
                if (bodyBuf.refCnt() > 0) {
                    bodyBuf.release(); // 释放slice引用
                }
            }
            // 循环处理下一个可能的帧
        }
    }
}
