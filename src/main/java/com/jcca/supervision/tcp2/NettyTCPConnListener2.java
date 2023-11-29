package com.jcca.supervision.tcp2;

import com.jcca.common.LogUtil;
import com.jcca.common.config.TcpConfig;
import com.jcca.util.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @description: 客户端启动时未连接成功则无限重连
 * @date: 2023/11/25 15:32
 * @author: sophia
 */
public class NettyTCPConnListener2 implements ChannelFutureListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private NettyTCPClient2 nettyTCPClient;

    public NettyTCPConnListener2(NettyTCPClient2 nettyTCPClient) {
        this.nettyTCPClient = nettyTCPClient;
    }

    @Override
    public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            TcpConfig tcpConfig = SpringUtil.getBean(TcpConfig.class);
            logger.error(LogUtil.buildLog(tcpConfig.getTcpIp() + ":" + tcpConfig.getTcpPort(),
                    "TCP2网络不通", (nettyTCPClient.getCount()) + "秒后重新连接"));
            Channel channel = future.channel();
            //任务(重连)  延迟时间   时间单位
            channel.eventLoop().schedule(() -> nettyTCPClient.connect(), nettyTCPClient.getCount(), TimeUnit.SECONDS);
        }
    }
}
