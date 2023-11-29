package com.jcca.supervision.controller;

import com.jcca.common.LogUtil;
import com.jcca.common.NettyBooter;
import com.jcca.common.config.TcpConfig;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.tcp.NettyTCPClient;
import com.jcca.util.SpringUtil;
import io.netty.channel.Channel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * @author sophia
 * @description 控制页面
 * @date 2023/11/28 13:40
 */
@Api(tags = "TCP控制页面")
@Controller
@RequestMapping("/tcp")
public class TcpController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private TcpConfig tcpConfig;


    @GetMapping("/startTcp")
    @ResponseBody
    @ApiOperation(value = "启动服务")
    public Object startTcp() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        if (Objects.nonNull(o) && "1".equals(o)) {
            return "不能重复启动TCP服务";
        }
        DataConst.TEMP_MAP.put(DataConst.NETTY_CHANNEL_FLAG, "1");
        logger.warn(LogUtil.buildLog("成功启动", "请参看运行情况"));

        // 启动客户端
        Executor taskExecutor = (Executor) SpringUtil.getBean("taskExecutor");
        taskExecutor.execute(() -> {
            NettyTCPClient tcpClient = new NettyTCPClient(tcpConfig.getTcpIp(), tcpConfig.getTcpPort());
            tcpClient.connect();
        });

        // 启动定时任务
        NettyBooter.SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(6);
        return "手动启动TCP服务成功";
    }

    @GetMapping("/stopTcp1")
    @ResponseBody
    @ApiOperation(value = "关闭服务")
    public static Object stopTcp1() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        if (Objects.isNull(o) || "0".equals(o)) {
            return "请先启动TCP一号服务";
        }
        DataConst.TEMP_MAP.put(DataConst.NETTY_CHANNEL_FLAG, "0");

        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (Objects.nonNull(channel) && channel.isActive()) {
            channel.close();

            NettyBooter.SCHEDULED_EXECUTOR_SERVICE.shutdown();
        }
        return "一号链接断开，断后不会自动重连直到再次手动启动";
    }


    @GetMapping("/stopTcp2")
    @ResponseBody
    @ApiOperation(value = "关闭二号链接")
    public static Object stopTcp2() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG2);
        if (Objects.isNull(o) || "0".equals(o)) {
            return "请先启动TCP一号服务";
        }
        DataConst.TEMP_MAP.put(DataConst.NETTY_CHANNEL_FLAG2, "0");

        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL2);
        if (Objects.nonNull(channel) && channel.isActive()) {
            channel.close();
        }
        return "二号链接断开，断后不会自动重连直到再次登陆成功";
    }
}
