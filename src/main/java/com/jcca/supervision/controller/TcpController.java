package com.jcca.supervision.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.LogUtil;
import com.jcca.common.NettyBooter;
import com.jcca.common.RedisService;
import com.jcca.common.ResultVo;
import com.jcca.common.config.TcpConfig;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.frame.*;
import com.jcca.supervision.entity.Device;
import com.jcca.supervision.entity.Nodes;
import com.jcca.supervision.entity.Station;
import com.jcca.supervision.service.DeviceService;
import com.jcca.supervision.service.NodesService;
import com.jcca.supervision.service.StationService;
import com.jcca.supervision.tcp.NettyTCPClient;
import com.jcca.util.AppPattenUtils;
import com.jcca.util.ResultVoUtil;
import com.jcca.util.SpringUtil;
import io.netty.channel.Channel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * @author sophia
 * @description 控制页面
 * @date 2023/11/28 13:40
 */
@RestController
@Api(tags = "TCP控制页面")
@RequestMapping("/tcp")
public class TcpController {

    @Value("${pushUrl}")
    private String pushUrl;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private TcpConfig tcpConfig;
    @Resource
    private RedisService redisService;
    @Resource
    private NodesService nodesService;
    @Resource
    private DeviceService deviceService;
    @Resource
    private StationService stationService;


    /**
     * 同步指定节点的下层节点
     */
    @GetMapping("/getAsset")
    @ApiOperation(value = "同步设备节点1111111111111111111111111")
    public ResultVo<List<String>> getAsset() throws InterruptedException {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        redisService.set(DataConst.DH_NODE_ID_LEVEL, "3");
        List<Station> list = stationService.list();
        for (Station station : list) {
            String stationId = station.getStationId();
            redisService.set(DataConst.DH_NODE_ID, stationId);
            //获取此节点的信息
            channel.writeAndFlush(GetSubstructFrame.newInstance(stationId));
        }
        Thread.sleep(2000);
        List<String> deviceIds = nodesService.getNodesIdByType("3");
        for (String id : deviceIds) {
            channel.writeAndFlush(GetPrpertyFrame.newInstance(id));
        }
        Thread.sleep(2000);
        //调取ITSM接口
        String body = HttpRequest.post(pushUrl + "/api/free/syslog/pullAllDevice").setReadTimeout(5000).setConnectionTimeout(5000).execute().body();
        return ResultVoUtil.success("共推送" + deviceIds.size() + "台设备");
    }


    /**
     * 同步指定节点的下层节点
     */
    @GetMapping("/getAllNodes2")
    @ApiOperation(value = "同步所有节点(超时没关系)")
    public ResultVo<List<String>> getAllNodes2() throws InterruptedException {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }

        for (int i = 0; i < 4; i++) {
            //获取指定节点的所有下层节点信息
            QueryWrapper<Nodes> qw = new QueryWrapper<>();
            qw.eq("type", i);
            List<Nodes> list = nodesService.list(qw);
            if (list.isEmpty()) {
                Nodes nodes1 = new Nodes();
                nodes1.setId("0");
                list.add(nodes1);
            }
            for (Nodes nodes : list) {
                //获取此节点的信息
                String nodeId = nodes.getId();
                Thread.sleep(500);
                redisService.set(DataConst.DH_NODE_ID, nodeId);
                redisService.set(DataConst.DH_NODE_ID_LEVEL, i + 1 + "");
                channel.writeAndFlush(GetSubstructFrame.newInstance(nodeId));
            }
            Thread.sleep(500);
        }
        return ResultVoUtil.success("执行成功");
    }


    /**
     * 同步动环的设备
     */
    @GetMapping("/pullAllDevice2")
    @ApiOperation(value = "推送动环所有设备")
    public ResultVo pullAllDevice2() {

        List<Device> list = deviceService.list();
        if (!list.isEmpty()) {
            //调取ITSM接口
            String body = HttpRequest.post(pushUrl + "/api/free/syslog/pullAllDevice").setReadTimeout(5000).setConnectionTimeout(5000).execute().body();
            return ResultVoUtil.success("共推送" + list.size() + "台设备");
        }

        return ResultVoUtil.success("无设备可推送");
    }


    /**
     * 同步动环的设备
     */
    @GetMapping("/pullAllDevice/{nodeId}")
    @ApiOperation(value = "同步并推送动环所有设备")
    public ResultVo pullAllDevice() throws InterruptedException {

        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);

        if (!channel.isActive()) {
            return ResultVoUtil.success("通道未开启,请先启动服务~");
        }

        List<String> ids = nodesService.getNodesIdByType("3");
        for (String id : ids) {
            Thread.sleep(500);
            channel.writeAndFlush(GetPrpertyFrame.newInstance(id));
        }
        List<Device> list = deviceService.list();
        if (!list.isEmpty()) {
            //调取ITSM接口
            String body = HttpRequest.post(pushUrl + "/api/free/syslog/pullAllDevice").setReadTimeout(5000).setConnectionTimeout(5000).execute().body();
            return ResultVoUtil.success("共推送" + list.size() + "台设备");
        }

        return ResultVoUtil.success("无设备可推送");
    }

    /**
     * 设置实时告警方式
     */
    @GetMapping("/setAlarmMode/{mode}")
    @ApiOperation(value = "设置实时告警方式")
    public ResultVo<List<String>> setAlarmMode(@PathVariable String mode) throws InterruptedException {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        channel.writeAndFlush(LoginDataFrame.newInstance());
        channel.writeAndFlush(GetActiveAlarmFrame.newInstance());
        Thread.sleep(5000);
        channel.writeAndFlush(SetAlarmModeFrame.newInstance("3"));
        return ResultVoUtil.success();
    }


    /**
     * 实时获取数据属性
     */
    @GetMapping("/getProperty")
    @ApiOperation(value = "获取设备属性")
    public ResultVo<List<String>> getProperty(String id) {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        channel.writeAndFlush(GetPrpertyFrame.newInstance(id));
        return ResultVoUtil.success("配置成功");

    }


    /**
     * 获取所有属性值
     */
    @GetMapping("/getAllProperty")
    @ApiOperation(value = "获取所有属性值")
    public ResultVo getAllProperty() throws InterruptedException {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        List<String> ids = nodesService.getNodesIdByType("4");

        for (String id : ids) {
            channel.writeAndFlush(GetPrpertyFrame.newInstance(id));
            Thread.sleep(500);

        }
        return ResultVoUtil.success("共获取" + ids.size() + "个属性值");
    }


    /**
     * 同步指定节点的子节点
     */
    @GetMapping("/getNodes/{nodeId}")
    @ApiOperation(value = "获取指定节点的下层子节点")
    public ResultVo<List<String>> getNodes(@PathVariable String nodeId) {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);

        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        //获取此节点的信息
        redisService.set(DataConst.DH_NODE_ID, nodeId);
        //获取指定节点的子节点信息
        channel.writeAndFlush(GetSubstructFrame.newInstance(nodeId));
        List<String> nodes = nodesService.getNodesByParentId(nodeId);
        return ResultVoUtil.success(nodes);
    }


    /**
     * 实时获取数据属性
     */
    @GetMapping("/getAlarm")
    @ApiOperation(value = "获取当前告警")
    public ResultVo<List<String>> getAlarm() throws InterruptedException {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        channel.writeAndFlush(GetActiveAlarmFrame.newInstance());
        return ResultVoUtil.success("获取成功");
    }


    /**
     * 设置属性数据实时获取方式
     */
    @GetMapping("/setDynProperty")
    @ApiOperation(value = "设置属性数据实时获取方式")
    public ResultVo<List<String>> setDynProperty(String secondsStr, String ids) {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        if (!AppPattenUtils.isNumber(secondsStr) || Integer.getInteger(secondsStr) < 15) {
            return ResultVoUtil.error("传入正确的间隔秒数,不可小于15秒");
        }
        Integer seconds = Integer.parseInt(secondsStr);

        channel.writeAndFlush(SetDynAccessModeFrame.newInstance(seconds, ids));
        return ResultVoUtil.success("配置成功");
    }


    /**
     * 查询TCP状态
     */
    @ApiOperation(value = "查询TCP状态")
    @GetMapping("/serialStatus")
    public String serialStatus() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        if (Objects.nonNull(o) && "1".equals(o)) {
            return "true";
        }
        return "false";
    }


    @GetMapping("/startTcp1")
    @ApiOperation(value = "启动服务")
    public ResultVo startTcp1() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        if (Objects.nonNull(o) && "1".equals(o)) {
            return ResultVoUtil.error("不能重复启动TCP服务");
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
        return ResultVoUtil.success("手动启动TCP服务成功");
    }

    @GetMapping("/stopTcp1")
    @ApiOperation(value = "关闭服务")
    public static ResultVo stopTcp1() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG);
        if (Objects.isNull(o) || "0".equals(o)) {
            return ResultVoUtil.error("请先启动TCP一号服务");
        }
        DataConst.TEMP_MAP.put(DataConst.NETTY_CHANNEL_FLAG, "0");

        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (Objects.nonNull(channel) && channel.isActive()) {
            channel.close();

            NettyBooter.SCHEDULED_EXECUTOR_SERVICE.shutdown();
        }
        return ResultVoUtil.success("一号链接断开，断后不会自动重连直到再次手动启动");
    }


    @GetMapping("/stopTcp2")
    @ApiOperation(value = "关闭二号链接")
    public static ResultVo stopTcp2() {
        Object o = DataConst.TEMP_MAP.get(DataConst.NETTY_CHANNEL_FLAG2);
        if (Objects.isNull(o) || "0".equals(o)) {
            return ResultVoUtil.error("请先启动TCP二号服务");
        }
        DataConst.TEMP_MAP.put(DataConst.NETTY_CHANNEL_FLAG2, "0");

        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL2);
        if (Objects.nonNull(channel) && channel.isActive()) {
            channel.close();
        }
        return ResultVoUtil.success("二号链接断开，断后不会自动重连直到再次登陆成功");
    }

    /**
     * 同步指定节点的所有下层节点
     */
    @GetMapping("/getAllNodes/{nodeId}")
    @ApiOperation(value = "获取指定节点的所有下层节点")
    public ResultVo<List<String>> getAllNodes(@PathVariable String nodeId) {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (!channel.isActive()) {
            return ResultVoUtil.error("动环程序未启动");
        }
        //获取此节点的信息
        redisService.set(DataConst.DH_NODE_ID, nodeId);
        //获取指定节点的所有子孙节点信息
        channel.writeAndFlush(GetNodesFrame.newInstance(nodeId));

        List<String> nodes = nodesService.getNodesByParentId(nodeId);
        //所有子孙节点
        List<String> allNode = nodes;
        for (int i = 1; i <= 3; i++) {
            //每次循环的当层节点
            List<String> nodeList = new ArrayList<>();
            for (String id : nodes) {
                List<String> nodes2 = nodesService.getNodesByParentId(id);
                if (ObjectUtil.isNotNull(nodes2) && !nodes2.isEmpty()) {
                    nodeList.addAll(nodes2);
                    allNode.addAll(nodes2);
                }
            }
            nodes = nodeList;
        }
        return ResultVoUtil.success(allNode);
    }

}
