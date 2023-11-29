package com.jcca.supervision.tcp2;

import cn.hutool.json.JSONUtil;
import com.jcca.common.LogUtil;
import com.jcca.common.config.AsyncConfig;
import com.jcca.common.config.TcpConfig;
import com.jcca.common.constant.BrokerConst;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.frame.HeartbeatFrame;
import com.jcca.supervision.handle.TcpResponseHandler;
import com.jcca.util.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description: TCP客户端业务处理器
 * @date: 2023/11/28 11:04
 * @author: sophia
 */
public class NettyTCPClientHandler2 extends SimpleChannelInboundHandler<DataBaseInfo> {

    private Logger logger = LoggerFactory.getLogger(NettyTCPClientHandler2.class);

    private NettyTCPClient2 nettyTCPClient;

    public NettyTCPClientHandler2(NettyTCPClient2 nettyTCPClient) {
        this.nettyTCPClient = nettyTCPClient;
    }

    /**
     * 链接成功
     * 心跳处理
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        nettyTCPClient.resetCount();
        logger.info(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "TCP连接成功", ctx.channel().id().toString()));
        sendHeartbeat(ctx);
    }

    private void sendHeartbeat(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        AsyncConfig.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (channel.isActive()) {
                ctx.writeAndFlush(HeartbeatFrame.newInstance());
            }

        }, 0L, DataConst.HEART_PERIOD_15, TimeUnit.SECONDS);// 启动就开始发送心跳，间隔15秒
    }

    /**
     * 接收消息统一处理
     *
     * @param ctx
     * @param msg 经过解码之后收到的对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBaseInfo msg) {
        logger.info(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "收到TCP解码后内容", JSONUtil.toJsonStr(msg)));

        // 交给处理器适配器进行处理
        TcpResponseHandler handler = SpringUtil.getBean(TcpResponseHandler.class);
        handler.handle(msg.getCode(), msg);
        ctx.flush();
    }


    /**
     * 断开链接时的处理
     *
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //查看是否主动断开链接
        Object o = BrokerConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        if (Objects.nonNull(o) && "0".equals(o)) {
            // 主动断开则 不再尝试重连
            return;
        }
        TcpConfig tcpConfig = SpringUtil.getBean(TcpConfig.class);
        logger.error(LogUtil.buildLog(tcpConfig.getTcpIp() + ":" + tcpConfig.getTcpPort(),
                "TCP连接中断", "马上重新连接"));
        nettyTCPClient.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
