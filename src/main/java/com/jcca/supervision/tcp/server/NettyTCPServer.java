package com.jcca.supervision.tcp.server;

import com.jcca.common.LogUtil;
import com.jcca.supervision.tcp.NettyTCPDecoder;
import com.jcca.supervision.tcp.NettyTCPEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.StandardSocketOptions;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description netty服务端，用于测试
 * @Date 2023/11/23 10:06
 * @Author sophia
 */
public class NettyTCPServer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int port;

    private ServerBootstrap serverBootstrap;

    public static Map<String, ChannelHandlerContext> serverChannelMap = new HashMap<>();

    public NettyTCPServer(int port) {
        this.port = port;
        init();
    }

    private void init() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, work)
                .option(NioChannelOption.of(StandardSocketOptions.SO_KEEPALIVE), true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast("decoder", new NettyTCPDecoder())
                                .addLast("encoder", new NettyTCPEncoder())
                                .addLast(new NettyTCPServerHandler());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    public void connect() {

        try {
            serverBootstrap.bind(port).sync();
        } catch (Exception e) {
            logger.error(LogUtil.buildLog("TCP服务端连接关闭", "监听端口：" + port));
        }
    }
}
