package com.jcca.supervision.tcp;

import com.jcca.common.LogUtil;
import com.jcca.supervision.data.frame.BaseDataFrame;
import com.jcca.supervision.data.frame.HeartbeatFrame;
import com.jcca.supervision.data.frame.LoginDataFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sophia
 * @description 编码
 * @date 2023/11/29 9:08
 */
public class NettyTCPEncoder extends MessageToByteEncoder<BaseDataFrame> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, BaseDataFrame dataFrame, ByteBuf out) throws Exception {

        if (dataFrame instanceof LoginDataFrame) {
            out.writeByte(LoginDataFrame.LEN);
            //out.writeByte("序号");
            out.writeCharSequence(LoginDataFrame.getUserName(), CharsetUtil.US_ASCII);
            out.writeCharSequence(LoginDataFrame.getPassword(),CharsetUtil.US_ASCII);
            LoginDataFrame login = (LoginDataFrame) dataFrame;
            encodeLogin(login, out);

        } else if (dataFrame instanceof LoginDataFrame) {

        } else if (dataFrame instanceof HeartbeatFrame) {

        } else {
            logger.warn(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "不能发送非心跳消息内容", ByteBufUtil.hexDump(out)));
        }
    }

    private void encodeLogin(LoginDataFrame frame, ByteBuf out) {

    }

}
