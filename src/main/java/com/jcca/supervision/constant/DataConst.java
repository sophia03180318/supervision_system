package com.jcca.supervision.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sophia
 * @description 相关常量
 * @date 2023/11/23 15:36
 */
public interface DataConst {

    // 存放临时数据
    Map<String, Object> TEMP_MAP = new ConcurrentHashMap<>();

    // 最小消息长度 长度 + 报文序号 + 命令字 +内容 (心跳时内容为空)
    int MIN_MSG_LEN = 4 + 4 + 4;

    //消息类型代码
    int HEART_BEAT = 0x04B1; // 心跳消息
    int LOGIN = 0x65; // 登录      101
    int LOGIN_ACK = 0x66; // 登录响应   102
    int LOGOUT = 0x67; // 登出    103
    int LOGOUT_ACK = 0x68; // 登出响应  104
    int GET_NODES = 0xC9; // 请求节点以下的整个树的ID号     201
    int SET_NODES = 0xCA; // 树请求响应     202
    int GET_SUBSTRUCT = 0xCB; // 请求节点下一层的ID号    203
    int SET_SUBSTRUCT = 0xCC; // 子层请求响应           204
    int GET_PROPERTY = 0x012D; // 请求数据属性       301
    int SET_PROPERTY = 0x012E; // 请求属性响应        302
    int SET_DYN_ACCESS_MODE = 0x0191; // 请求实时数据方式设置  401
    int DYN_ACCESS_MODE_ACK = 0x0192; // 实时数据响应     402
    int SET_ALARM_MODE = 0x01F5; // 请求报警数据方式设置     501
    int ALARM_MODE_ACK = 0x01F6; // 报警方式设置响应         502
    int SEND_ALARM = 0x01F7; // 实时报警发送                 503
    int SEND_ALARM2 = 0x0318; // 实时报警发送                 503
    int GET_ACTIVE_ALARM = 0x01F8; // 请求所有当前报警        504
    int SET_ACTIVE_ALARM = 0x01F9; // 请求所有当前告警响应     505

/*
    String USER_NAME = "zhjc";   //用户名
    String PASSWORD = "123"; //口令
*/


    //登录权限
    int INVALID = 0; // 无权限
    int LEVEL1 = 1; // 读权限
    int LEVEL2 = 2; // 读写权限


    //Redis
    String DH_NODE_ID = "dhNodeId";//里面存储当前询问父节点的ID  可手动更新      String
    String DH_NODE_ID_LEVEL="dhNodeIdLevel"; ////里面存储当前询问的类型  可手动更新      String
    String DH_DEVICE_ID_LIST = "dhDeviceIdList";//里面存储当前询问父节点的子ID列表 List<String>

    String DH_NODE = "dhNode";//节点信息Key  里面存储所有节点信息 List<Nodes>
    String DH_PROERTY = "dhProperty";//属性信息Key  里面存储属性信息 <PropertyData>
    String DH_PROERTY_PARENT = "dhPropertyParent";//属性信息Key  里面存储属性所属的设备ID parentId


    // 心跳间隔15秒
    Long HEART_PERIOD_15 = 15L; // 心跳间隔15秒
    String MSG_HEART_BEAT = "04b"; // 心跳内容



    // netty通道key
    String NETTY_CHANNEL_FLAG = "NettyChannelFlag"; // 手动启动关闭用
    String NETTY_TCP_CHANNEL = "NettyTCPChannel"; // 使用中的通道
    String NETTY_SERVER_CHANNEL = "NettyServerChannel"; // 测试用

    String NETTY_CHANNEL_FLAG2 = "NettyChannelFlag2"; // 手动启动关闭用
    String NETTY_TCP_CHANNEL2 = "NettyTCPChannel2"; // 使用中的通道


    //EnumType 监控数据种类
    int STATION = 0; //局、站
    int DEVICE = 1;  //设备
    int DI = 2;      //二态数字输入量
    int AI = 3;      //模拟输入量
    int DO = 4;      //数字输出量
    int AO = 5;      //模拟输出量
    int STRIN = 6;   //字符串量


    //EnumAlarmLevel  EnumState  EnumAlarmMode 告警等级  数据值的状态  告警等级设定的模式
    int NOALARM = 0;      //无告警判断
    int FATAL = 1;      //严重告警
    int MAIN = 2;      //主要告警
    int NORMAL = 3;      //一般告警
    int OPEVENT = 4;      //操作事件
    int INVALID2 = 5;      //无效事件


    //EnumEnable    是 能
    int DISABLE = 0;    //禁止/不能
    int ENABLE = 1;     //开放/能


    //EnumDeviceType  设备类型
    int HI_DISTRIBUTER = 0;   //高压配电设备
    int LO_DISTRIBUTER = 1;   //低压配电设备
    int DIESEL_GENERATOR = 2;   //柴油发电机组
    int GAS_GENERATOR = 3;   //燃气发电机组
    int UPS = 4;   //UPS
    int DC_AC = 5;   //逆变器
    int RECTIFIER = 6;   //整流配电设备
    int SOLAR = 7;   //太阳能供电设备
    int DC_DC = 8;   //DC-DC变换器
    int WIND_GENERATOR = 9;   //风力发电设备
    int BATTERY = 10;   //蓄电池组
    int LOCAL_AIRCONDITION = 11;   //局部空调设备
    int LOCAL_AIRCONDITION2 = 12;   //集中空调设备
    int DOOR_FORCE = 13;   //门禁
    int ENVIORMENT = 14;   //环境设备
    int LIGHTNINGPROOF = 15;   //防雷设备


    //EnumAccessMode    实时数据访问的方式
    int ASK_ANSWER = 0;      //一问一答方式
    int CHANGE_TRIGGER = 1;      //改变时自动发送数据方式
    int TIME_TRIGGER = 2;      //定时发送数据方式
    int STOP = 3;      //停止发送数据方式


    //EnumResult    设置结果
    int FAILURE = 0;    //失败
    int SUCCESS = 1;    //成功

}
