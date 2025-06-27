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

public class NettyTCPDecoder33 extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(NettyTCPDecoder.class);
    private static final int HEADER_LENGTH = 12; // 长度(4) + 序号(4) + 类型(4)
    private static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024; // 最大10MB
    private static final int HEARTBEAT_TYPE = 0x04B2;
    private static final int ALARM_TYPE = 0x01F7; // 告警类型
    private static final int ALARM_HEADER_LENGTH = 4; // 告警数量字段长度
    private static final int DESCRIPTION_LENGTH = 160; // 描述填充至160字节
    private static final int CRLF_LENGTH = 2;       // \r\n
    private static final int SINGLE_ALARM_LENGTH = 4  // ID
            + 4                                    // Status
            + DESCRIPTION_LENGTH                   // 描述主体
            + CRLF_LENGTH;                         // 回车换行

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            while (in.readableBytes() >= HEADER_LENGTH) {
                in.markReaderIndex();

                // 1. 读取报文头
                int totalLength = in.readInt();
                long serialNo = in.readUnsignedInt();
                int command = in.readInt();

                // 2. 校验长度
                if (totalLength < HEADER_LENGTH || totalLength > MAX_FRAME_LENGTH) {
                    logger.warn("非法报文长度: {} (应在 {}-{} 之间)", totalLength, HEADER_LENGTH, MAX_FRAME_LENGTH);
                    // 丢弃不合理长度字段后续字节
                    in.resetReaderIndex();
                    in.skipBytes(4);
                    continue;
                }

                int contentLength = totalLength - HEADER_LENGTH;
                if (in.readableBytes() < contentLength) {
                    // 数据不全，等待下次读取
                    in.resetReaderIndex();
                    return;
                }

                // 3. 处理告警报文
                if (command == ALARM_TYPE) {
                    // 3.1 读取告警数量
                    int alarmCount = in.readInt();
                    int bodyLength = ALARM_HEADER_LENGTH + alarmCount * SINGLE_ALARM_LENGTH;

                    if (contentLength < bodyLength) {
                        // 内容长度与告警数据总长不符，回退
                        in.resetReaderIndex();
                        return;
                    }

                    // 3.2 读取告警内容
                    ByteBuf alarmBuf = in.readSlice(bodyLength);
                    // 3.3 跳过 padding 字节
                    int padding = contentLength - bodyLength;
                    if (padding > 0) {
                        in.skipBytes(padding);
                    }

                    // 3.4 解码并处理告警数据
                    TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
                    Object obj = handler.decode(command, alarmBuf);
                    processDecodedObject(command, serialNo, obj, out);
                    logger.info("成功处理告警报文 [数量:{} 总长度:{}]", alarmCount, totalLength);
                    continue;
                }

                // 4. 处理心跳
                if (command == HEARTBEAT_TYPE) {
                    logger.info("收到TCP心跳");
                    in.skipBytes(contentLength);
                    continue;
                }

                // 5. 处理普通业务报文
                ByteBuf contentBuf = in.readSlice(contentLength);
                processBusinessPacket(command, serialNo, contentBuf, out);
            }
        } catch (Exception e) {
            logger.error("报文解析异常: {}", e.getMessage(), e);
            in.resetReaderIndex();
        }
    }

    private void processBusinessPacket(int command, long serialNo, ByteBuf contentBuf, List<Object> out) {
        TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
        Object obj = handler.decode(command, contentBuf);
        processDecodedObject(command, serialNo, obj, out);
    }

    private void processDecodedObject(int command, long serialNo, Object obj, List<Object> out) {
        if (obj instanceof DataBaseInfo) {
            DataBaseInfo baseInfo = (DataBaseInfo) obj;
            baseInfo.setCode(command);
            baseInfo.setNum(serialNo);
            baseInfo.setTime(new Date());
            out.add(baseInfo);
        } else if (obj != null) {
            out.add(obj);
        }
    }

    /**
     * 格式化字节为十六进制字符串，空格分隔。
     */
    private static String formatBinaryData(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
