package com.jcca.common;

import com.jcca.common.config.TcpConfig;
import com.jcca.common.constant.BrokerConst;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.tcp.NettyTCPClient;
import com.jcca.util.SpringUtil;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @description: 启动程序后连接指定服务器
 * @date: 2023/11/28 11:17
 * @author: sophia
 */
@Component
@DependsOn("applicationContextRegister")
public class NettyBooter {

    public static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;

    @Resource
    private TcpConfig tcpConfig;

    @PostConstruct
    public void run() {
        Executor taskExecutor = (Executor) SpringUtil.getBean("taskExecutor");
        this.startTieke(taskExecutor);
    }


    // 启动TCP服务监听
    private void startTieke(Executor taskExecutor) {
        if (tcpConfig.getTcpOpen() == 1) {
            BrokerConst.TEMP_MAP.put(DataConst.NETTY_CHANNEL_FLAG, "1");
            taskExecutor.execute(() -> {
                NettyTCPClient tcpClient = new NettyTCPClient(tcpConfig.getTcpIp(), tcpConfig.getTcpPort());
                tcpClient.connect();
            });

           /* // 启动定时任务  检查是否超时
            Long timeout = tcpConfig.getTcpReceiveTimeout();
            SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(6);
            SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(new TiekeWorkstateRunnable(), timeout, timeout * 2 + 1, TimeUnit.SECONDS);
           SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(new TiekeChannelRunnable(), timeout, timeout * 2 + 1, TimeUnit.SECONDS);
            SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(new TiekeSecureLinkRunnable(), timeout, timeout * 2 + 1, TimeUnit.SECONDS);*/
        }

        // 启动TCP服务端 用于测试
/*        if (tcpConfig.getTcpServerTest() == 1) {
            taskExecutor.execute(() -> {
                TiekeNettyTCPServer tcpServer = new TiekeNettyTCPServer(tcpConfig.getTcpPort());
                tcpServer.connect();
            });
        }*/
    }


}
