package com.jcca.supervision.tcp2;

import cn.hutool.core.util.IdUtil;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.tcp.NettyTCPDecoder;
import com.jcca.supervision.tcp.NettyTCPEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.StandardSocketOptions;

/**
 * @description: 接收客户端2
 * @date: 2023/11/22 11:03
 * @author: sophia
 */
public class NettyTCPClient2 {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 重连计时器，秒，重连时间逐渐增长，每次增加2秒
    private int count;

    private String host;

    private int port;

    private Bootstrap tcpBootStrap;

    public NettyTCPClient2(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    private void init() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        tcpBootStrap = new Bootstrap();
        tcpBootStrap.group(eventLoopGroup)
                .option(NioChannelOption.of(StandardSocketOptions.SO_KEEPALIVE), true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 2)
                .option(ChannelOption.SO_SNDBUF, 1024 * 2)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024 * 2))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast("decoder", new NettyTCPDecoder())
                                .addLast("encoder", new NettyTCPEncoder())
                                .addLast(new NettyTCPClientHandler2(NettyTCPClient2.this));
                    }
                });
    }

    public void connect() {
        // 每轮10分钟
        if (count > 46) {
            count = 0;
        }
        count += 2;

        try {
            ChannelFuture channelFuture = tcpBootStrap.connect(host, port);
            channelFuture.addListener(new NettyTCPConnListener2(NettyTCPClient2.this));
            Channel channel = channelFuture.channel();

            DataConst.TEMP_MAP.put(DataConst.NETTY_TCP_CHANNEL2, channel);

            channel.closeFuture().sync();
        } catch (Exception e) {
            logger.error("[{}]-[TCP2客户端连接关闭]", IdUtil.getSnowflakeNextIdStr());
        }
    }

    public int getCount() {
        return count;
    }

    public void resetCount() {
        count = 0;
    }
}
