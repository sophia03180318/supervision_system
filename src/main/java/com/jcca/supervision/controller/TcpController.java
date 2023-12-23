package com.jcca.supervision.controller;

import cn.hutool.http.HttpRequest;
import com.jcca.common.LogUtil;
import com.jcca.common.NettyBooter;
import com.jcca.common.RedisService;
import com.jcca.common.config.TcpConfig;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.frame.HeartbeatFrame;
import com.jcca.supervision.entity.Nodes;
import com.jcca.supervision.tcp.NettyTCPClient;
import com.jcca.util.SpringUtil;
import io.netty.channel.Channel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
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

    @Value("${pushUrl}")
    private String pushUrl;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private TcpConfig tcpConfig;
    @Resource
    private RedisService redisService;

    /**
     * 向ITSM同步动环所有设备
     */
    @GetMapping("/pullAllDevice")
    @ResponseBody
    @ApiOperation(value = "向ITSM同步动环所有设备")
    public Object pullAllDevice() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        String station1 = tcpConfig.getStation1();
        String station2 = tcpConfig.getStation2();

        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);

        if (!channel.isActive()) {
            return "通道未开启,请先启动服务~";
        }
        //同步机房1的设备
        channel.writeAndFlush(HeartbeatFrame.newInstance());

        //同步机房2的设备
        channel.writeAndFlush(HeartbeatFrame.newInstance());

        //调取ITSM接口
        HttpRequest.post(pushUrl + "/api/free/syslog/saveLog").setReadTimeout(5000).setConnectionTimeout(5000).execute().body();
        return "同步成功~";
    }


    /**
     * 获取指定节点的子节点
     */
    @GetMapping("/getNodes/{nodeId}")
    @ApiOperation(value = "获取指定节点的子节点")
    public List<Nodes> getNodes(@PathVariable String nodeId) {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);

        if (!channel.isActive()) {
            return null;
        }
        //获取此节点的信息
        redisService.set(DataConst.DH_NODE_ID, nodeId);

        //获取指定节点的子节点信息
        channel.writeAndFlush(HeartbeatFrame.newInstance());
        return null;
    }

    @GetMapping("/startTcp1")
    @ResponseBody
    @ApiOperation(value = "启动服务")
    public Object startTcp1() {
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
