package com.jcca.supervision.tcp;

import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.TcpResponseHandler;
import com.jcca.util.SpringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @author sophia
 * @description 消息解码
 * @date 2023/11/23 9:08
 */
public class NettyTCPDecoder extends ByteToMessageDecoder {

    private long time = 0;
    private Logger logger = LoggerFactory.getLogger(getClass());
    // 报文头长度: 总长度(8) + 序号(8) + 类型(4) = 20字节
    private static final int HEADER_LENGTH = 20;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            // 检查是否足够解析头部
            if (in.readableBytes() < HEADER_LENGTH) {
                return;
            }

            // 标记当前读取位置
            in.markReaderIndex();

            // 读取报文头（大端序）
            long totalLength = in.readLong();
            long serialNo = in.readLong();
            int type = (int) in.readUnsignedInt(); // 4字节报文类型

            // 验证报文长度
            if (totalLength < HEADER_LENGTH || totalLength > 65535) {
                logger.warn("非法报文长度: {} (范围应在 {}-{})", totalLength, HEADER_LENGTH, 65535);
                in.skipBytes(in.readableBytes());
                return;
            }

            // 计算内容长度
            int contentLength = (int)(totalLength - HEADER_LENGTH);

            // 检查内容是否完整
            if (in.readableBytes() < contentLength) {
                in.resetReaderIndex();
                return;
            }

            // 交给适配器处理报文内容
            TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
            ByteBuf contentBuf = in.readSlice(contentLength);
            Object obj = handler.decode(type, contentBuf);

            // 处理解码结果
            processDecodedObject(type, serialNo, obj, out);

            // 循环处理粘包数据
            while (in.readableBytes() >= HEADER_LENGTH) {
                totalLength = in.readLong();
                serialNo = in.readLong();
                type = (int) in.readUnsignedInt();// 报文类型
                contentLength = (int)(totalLength - HEADER_LENGTH);
                if (in.readableBytes() < contentLength) {
                    in.resetReaderIndex();
                    break;
                }
                contentBuf = in.readSlice(contentLength);
                obj = handler.decode(type, contentBuf);
                processDecodedObject(type, serialNo, obj, out);
            }

        } catch (Exception e) {
            logger.error("报文解析异常: {}", e.getMessage(), e);
            in.resetReaderIndex();
        }
    }
    private void processDecodedObject(int type, long serialNo, Object obj, List<Object> out) {
        if (obj instanceof DataBaseInfo) {
            DataBaseInfo baseInfo = (DataBaseInfo) obj;
            baseInfo.setCode(type);
            baseInfo.setNum(serialNo);
            baseInfo.setTime(new Date());
            out.add(baseInfo);
        } else if (obj != null) {
            logger.info("收到非DataBaseInfo类型对象: {}", obj.getClass().getSimpleName());
            out.add(obj);
        }
    }

}
