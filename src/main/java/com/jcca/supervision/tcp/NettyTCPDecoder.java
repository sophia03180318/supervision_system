package com.jcca.supervision.tcp;

import com.jcca.common.LogUtil;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.TcpResponseHandler;
import com.jcca.util.SpringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author sophia
 * @description 消息解码
 * @date 2023/11/23 9:08
 */
public class NettyTCPDecoder extends ByteToMessageDecoder {

    private long time = 0;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String hexDump = ByteBufUtil.hexDump(in);
        if (hexDump.length() < 24) {
            logger.warn(LogUtil.buildLog("丢弃过短的消息，长度为 ", hexDump));
            in.clear();
        }
        // 排除心跳打印
        if (hexDump.substring(20).startsWith(DataConst.MSG_HEART_BEAT)) {
            logger.info(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "收到TCP心跳", "砰"));
            in.clear();
        } else {
            logger.info(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "收到TCP原始内容", hexDump));
            decodeFrame(in, out);
        }
    }

    private synchronized void decodeFrame(ByteBuf in, List<Object> out) {
        //readableBytes  读取数据长度
        int allLen = in.readableBytes();

        //最小消息长度
        if (allLen < DataConst.MIN_MSG_LEN) {
            logger.warn(LogUtil.buildLog("丢弃过短的消息，长度为 ", allLen + ""));
            in.clear();
            return;
        }

        long l = in.readUnsignedInt();// 报文长度
        long num = in.readUnsignedInt();// 报文序号
        Integer type = Integer.parseInt(String.valueOf(in.readUnsignedInt()));// 报文类型
        if (allLen == l) {
            // 交给适配器处理
            TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
            Object obj = handler.decode(type, in);
            //获取解码后的对象
            if (Objects.nonNull(obj) && obj instanceof DataBaseInfo) {
                DataBaseInfo baseInfo = (DataBaseInfo) obj;
                baseInfo.setCode(type);
                baseInfo.setNum(num);
                baseInfo.setTime(new Date());
                out.add(baseInfo);
            }

        } else {
            if (type == 0x01F7) {
                time = System.currentTimeMillis();
                TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
                Object obj = handler.decode(type, Unpooled.copiedBuffer(in));
                if (Objects.nonNull(obj) && obj instanceof DataBaseInfo) {
                    DataBaseInfo baseInfo = (DataBaseInfo) obj;
                    baseInfo.setCode(type);
                    baseInfo.setNum(num);
                    baseInfo.setTime(new Date());
                    out.add(baseInfo);
                }
            } else {
                if ( System.currentTimeMillis()-time < 3000) {
                    TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
                    Object obj = handler.decode(0x0318, Unpooled.copiedBuffer(in));
                    if (Objects.nonNull(obj) && obj instanceof DataBaseInfo) {
                        DataBaseInfo baseInfo = (DataBaseInfo) obj;
                        baseInfo.setCode(0x01F7);
                        baseInfo.setNum(num);
                        baseInfo.setTime(new Date());
                        out.add(baseInfo);
                    }
                }else {
                    in.clear();
                }
            }

        }
    }

}
