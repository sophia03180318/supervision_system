package com.jcca.supervision.tcp.server;

import com.jcca.common.LogUtil;
import com.jcca.supervision.constant.DataConst;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description netty服务端处理器，用于测试
 * @Date 2023/12/6 10:23
 * @Author sophia
 */
public class NettyTCPServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        logger.info(LogUtil.buildLog("TCP服务端收到客户端消息", ByteBufUtil.hexDump(byteBuf)));
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        NettyTCPServer.serverChannelMap.put(DataConst.NETTY_SERVER_CHANNEL, ctx);
        logger.info(LogUtil.buildLog("TCP服务端已激活", ctx.channel().id().toString()));
    }
}
