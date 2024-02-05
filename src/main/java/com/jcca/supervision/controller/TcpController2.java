package com.jcca.supervision.controller;

import org.springframework.stereotype.Controller;

/**
 * @author sophia
 * @description 控制页面
 * @date 2023/11/28 13:40
 */
@Controller
public class TcpController2 {
/*
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


    *//**
     * 初始页面
     *//*
    @ApiOperation(value = "初始页面")
    @GetMapping("/")
    public String index(Model model, Device device,Integer size, Integer page) {
        IPage iPage = PagePlugin.startPage(1, 100);
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(device.getName()) && !device.getName().isEmpty()) {
            wrapper.like("NAME", device.getName());
        }
        if (ObjectUtil.isNotNull(device.getDeviceType())&&device.getDeviceType()!=99) {
            wrapper.eq("DEVICE_TYPE", device.getDeviceType());
        }
        iPage = deviceService.page(iPage, wrapper);
        List<Device> records = iPage.getRecords();
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/index";
    }


    *//**
     * 查询TCP状态
     *//*
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
    @ResponseBody
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
    @ResponseBody
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
    @ResponseBody
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
    }*/
}
