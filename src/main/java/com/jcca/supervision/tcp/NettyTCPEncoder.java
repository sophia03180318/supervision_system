package com.jcca.supervision.tcp;

import com.jcca.common.LogUtil;
import com.jcca.supervision.data.frame.BaseDataFrame;
import com.jcca.supervision.data.frame.HeartbeatFrame;
import com.jcca.supervision.data.frame.LoginDataFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Random;

/**
 * @author sophia
 * @description 编码
 * @date 2023/11/29 9:08
 */
public class NettyTCPEncoder extends MessageToByteEncoder<BaseDataFrame> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, BaseDataFrame dataFrame, ByteBuf out) throws Exception {
        Random random = new Random();
        if (dataFrame instanceof LoginDataFrame) {
            out.writeByte(LoginDataFrame.LEN);
            out.writeByte(random.nextInt());
            out.writeByte(LoginDataFrame.DATA_TYPE);
            out.writeCharSequence(LoginDataFrame.getUserName(), Charset.forName("GBK"));
            out.writeCharSequence(LoginDataFrame.getPassword(), Charset.forName("GBK"));
            LoginDataFrame login = (LoginDataFrame) dataFrame;
            encode(login, out);

        } else if (dataFrame instanceof HeartbeatFrame) {
            out.writeByte(HeartbeatFrame.LEN);
            out.writeByte(random.nextInt());
            out.writeByte(HeartbeatFrame.DATA_TYPE);
            HeartbeatFrame heart = (HeartbeatFrame) dataFrame;
            encode(heart, out);

        } else if (dataFrame instanceof HeartbeatFrame) {

        } else {
            logger.warn(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "不能发送非心跳消息内容", ByteBufUtil.hexDump(out)));
        }
    }


    /**
     * 心跳
     */
    private void encode(HeartbeatFrame frame, ByteBuf out) {

    }

    /**
     * 登录
     */
    private void encode(LoginDataFrame frame, ByteBuf out) {

    }

}
