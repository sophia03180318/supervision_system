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

public class NettyTCPDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(NettyTCPDecoder.class);
    private static final int HEADER_LENGTH = 12; // 长度(4) + 序号(4) + 类型(4)
    private static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024; // 提升到10MB
    private static final int HEARTBEAT_TYPE = 0x04B2;
    private static final int ALARM_TYPE = 0x01F7; // 新增告警类型
    private static final int ALARM_HEADER_LENGTH = 4; // 告警数量字段长度
    private static final int SINGLE_ALARM_LENGTH = 168; // 单个告警长度

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            while (in.readableBytes() >= HEADER_LENGTH) {
                // 保存头部开始位置
                in.markReaderIndex();

                // 1. 读取头部字段
                int totalLength = in.readInt();
                long serialNo = in.readUnsignedInt();
                int command = in.readInt();
                int contentLength = totalLength - HEADER_LENGTH;

                // 2. 验证长度
                if (totalLength < HEADER_LENGTH || totalLength > MAX_FRAME_LENGTH) {
                    logger.warn("非法报文长度: {} (范围应为 {}-{})",
                            totalLength, HEADER_LENGTH, MAX_FRAME_LENGTH);
                    in.skipBytes(in.readableBytes());
                    continue;
                }

                // 3. 检查是否为告警类型
                if (command == ALARM_TYPE) {
                    handleAlarmPacket(in, out, totalLength, serialNo, command, contentLength);
                    continue;
                }

                // 4. 检查内容完整性（常规报文）
                if (in.readableBytes() < contentLength) {
                    in.resetReaderIndex();
                    return;
                }

                // 5. 处理心跳报文
                if (command == HEARTBEAT_TYPE) {
                    handleHeartbeat(serialNo, out);
                    in.skipBytes(contentLength);
                    continue;
                }

                // 6. 处理常规业务报文
                ByteBuf contentBuf = in.readSlice(contentLength);
                processBusinessPacket(command, serialNo, contentBuf, out);
            }
        } catch (Exception e) {
            logger.error("报文解析异常: {}", e.getMessage(), e);
            in.resetReaderIndex();
        }
    }

    // 处理告警报文
    private void handleAlarmPacket(ByteBuf in, List<Object> out,
                                   int totalLength, long serialNo,
                                   int command, int contentLength) {
        // 检查是否有足够的数据读取告警数量字段
        if (in.readableBytes() < ALARM_HEADER_LENGTH) {
            in.resetReaderIndex();
            return;
        }

        // 读取告警数量
        int alarmCount = in.readInt();
        int expectedLength = ALARM_HEADER_LENGTH + (alarmCount * SINGLE_ALARM_LENGTH);

        // 验证长度一致性
        if (contentLength != expectedLength) {
            logger.warn("告警报文长度不一致");
        }else{
            logger.warn("头部长度={}, 计算长度={} (数量={})",
                    contentLength, expectedLength, alarmCount);
        }

        // 检查完整告警数据是否就绪
        int remainingAlarmLength = alarmCount * SINGLE_ALARM_LENGTH;
        if (in.readableBytes() < remainingAlarmLength) {
            // 回退到告警数量字段前（头部+告警数量）
            in.resetReaderIndex();
            return;
        }

        // 读取告警内容
        ByteBuf alarmContent = in.readSlice(remainingAlarmLength);

        // 处理告警数据
        TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
        Object obj = handler.decode(command, alarmContent);
        processDecodedObject(command, serialNo, obj, out);

        logger.info("成功处理告警报文 [数量:{} 总长度:{}]", alarmCount, totalLength);
    }

    // 心跳处理（保持不变）
    private void handleHeartbeat(long serialNo, List<Object> out) {
        logger.info("收到TCP心跳 [序号:{}]", serialNo);
    }

    // 业务报文处理
    private void processBusinessPacket(int command, long serialNo, ByteBuf contentBuf, List<Object> out) {
        TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
        Object obj = handler.decode(command, contentBuf);
        processDecodedObject(command, serialNo, obj, out);
    }

    // 对象处理
    private void processDecodedObject(int command, long serialNo, Object obj, List<Object> out) {
        if (obj instanceof DataBaseInfo) {
            DataBaseInfo baseInfo = (DataBaseInfo) obj;
            baseInfo.setCode(command);
            baseInfo.setNum(serialNo);
            baseInfo.setTime(new Date());
            out.add(baseInfo);
        } else if (obj != null) {
            logger.info("收到其他类型对象: {}", obj.getClass().getSimpleName());
            out.add(obj);
        }
    }

    /**
     * 字节数组转十六进制字符串（带空格分隔）
     * 示例输出：01 A3 FF 00
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.substring(0, sb.length() - 1); // 去除末尾空格
    }

    /**
     * 简单字节格式化（空格分隔）
     * 示例：00 00 00 0C 00 00 04 B1 00 00 00 01
     */
    private static String formatBinaryData(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b)); // 两位大写十六进制+空格
        }
        return sb.toString().trim(); // 去除末尾空格
    }
}
