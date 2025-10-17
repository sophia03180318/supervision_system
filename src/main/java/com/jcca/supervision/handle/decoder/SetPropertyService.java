package com.jcca.supervision.handle.decoder;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.PropertyData;
import com.jcca.supervision.entity.Device;
import com.jcca.supervision.entity.Property;
import com.jcca.supervision.entity.Station;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import com.jcca.supervision.service.DeviceService;
import com.jcca.supervision.service.PropertyService;
import com.jcca.supervision.service.StationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sophia
 * @description 获取指定 ID的属性值
 * @date 2023/11/28 10:43
 */
@Service
public class SetPropertyService implements ResponseHandleAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceService deviceService;
    @Resource
    private StationService stationService;
    @Resource
    private PropertyService propertyService;


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.SET_PROPERTY;
    }

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    @Override
    public Object decode(ByteBuf contentBuf) {
        DataBaseInfo baseInfo = new DataBaseInfo();
        ArrayList<PropertyData> properties = new ArrayList<>();
        int cnt = contentBuf.readInt();// 属性数量
        if (cnt == -1) {
            logger.info(LogUtil.buildLog("属性数量过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }
        if (cnt == -2) {
            logger.info(LogUtil.buildLog("无指定ID，获取属性失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }

        for (int i = 0; i < cnt; i++) {
            int type = contentBuf.readInt();// 属性类型
            long id = contentBuf.readUnsignedInt();// 数据ID
            long parentID = contentBuf.readUnsignedInt();// 设备ID
            String name = contentBuf.readCharSequence(40, Charset.forName("GBK")).toString().trim(); //名称
            String des = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim();// 数据描述

            PropertyData property = new PropertyData();
            property.setType(type);
            property.setPropertyId(String.valueOf(id));
            property.setParentID(String.valueOf(parentID));
            property.setName(name);
            property.setDescc(des);

            switch (type) {
                //STATION = 0  局、站
                case DataConst.STATION:
                    float longitude = contentBuf.readFloat(); //经度
                    float latitude = contentBuf.readFloat(); //纬度
                    property.setLongitude(longitude);
                    property.setLatitude(latitude);
                    break;

                //DEVICE = 1  设备
                case DataConst.DEVICE:
                    int deviceType = contentBuf.readInt();
                    String productor = contentBuf.readCharSequence(40, Charset.forName("GBK")).toString().trim(); //生产厂家
                    String version = contentBuf.readCharSequence(40, Charset.forName("GBK")).toString().trim(); //设备版本
                    //上架时间
                    short years = contentBuf.readShort();
                    String month = String.valueOf(contentBuf.readUnsignedByte());
                    String day = String.valueOf(contentBuf.readUnsignedByte());
                    String hour = String.valueOf(contentBuf.readUnsignedByte());
                    String minute = String.valueOf(contentBuf.readUnsignedByte());
                    String second = String.valueOf(contentBuf.readUnsignedByte());
                    String beginRunTime = String.valueOf(years) + "-" + getTimeStr(month) + "-" + getTimeStr(day) + " " + getTimeStr(hour) + ":" + getTimeStr(minute) + ":" + getTimeStr(second);
                    property.setDeviceType(deviceType);
                    property.setProductor(productor);
                    property.setVersion(version);
                    property.setBeginRunTime(beginRunTime);
                    break;

                /**
                 * DI = 2  二态数字输入量
                 * AlarmThresbhold	EnumEnable	告警触发阀值
                 * Alarmlevel	EnumAlarmLevel	告警等级
                 * AlarmEnable	EnumEnable 	告警使能标记
                 * Desc0	Char [DES_LENGTH]	数字值为0时的描述
                 * Desc1	Char [DES_LENGTH]	数字值为0时的描述
                 * Saved	EnumEnable 	是否保存
                 */
                case DataConst.DI:
                    int alarmThresbhold = contentBuf.readInt();
                    int alarmlevel = contentBuf.readInt();
                    int alarmEnable = contentBuf.readInt();
                    String des0 = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim();// 数据描述
                    String des1 = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim();// 数据描述
                    int saved = contentBuf.readInt();
                    property.setAlarmThresbhold(alarmThresbhold);
                    property.setAlarmlevel(alarmlevel);
                    property.setAlarmEnable(alarmEnable);
                    property.setDesc0(des0);
                    property.setDesc1(des1);
                    property.setSaved(saved);
                    break;

                /**
                 * AI = 3    模拟输入量
                 * MaxVal	float  	有效上限
                 * MinVal	float  	有效下限
                 * Alarmlevel	EnumAlarmLevel	告警等级
                 * AlarmEnable	EnumEnable	告警使能标记
                 * HiLimit1	float  	一级告警上限
                 * LoLimit1	float  	一级告警下限
                 * HiLimit2	float  	二级告警上限
                 * LoLimit2	float  	二级告警下限
                 * HiLimit3	float  	三级告警上限
                 * LoLimit3	float 	三级告警下限
                 * Stander	float  	标称值
                 * Percision	float  	精度
                 * Saved	EnumEnable 	是否保存历史
                 * Unit	char [UNIT_LENGTH]	单位
                 * */
                case DataConst.AI:
                    float maxVal = contentBuf.readFloat();
                    float minVal = contentBuf.readFloat();
                    int alarmlevel3 = contentBuf.readInt();
                    int alarmEnable3 = contentBuf.readInt();
                    float hiLimit1 = contentBuf.readFloat();
                    float loLimit1 = contentBuf.readFloat();
                    float hiLimit2 = contentBuf.readFloat();
                    float loLimit2 = contentBuf.readFloat();
                    float hiLimit3 = contentBuf.readFloat();
                    float loLimit3 = contentBuf.readFloat();
                    float stander = contentBuf.readFloat();
                    float percision = contentBuf.readFloat();
                    int saved3 = contentBuf.readInt();
                    String unit = contentBuf.readCharSequence(8, Charset.forName("GBK")).toString().trim(); //设备版本
                    property.setMaxVal(maxVal);
                    property.setMinVal(minVal);
                    property.setAlarmlevel(alarmlevel3);
                    property.setAlarmEnable(alarmEnable3);
                    property.setHiLimit1(hiLimit1);
                    property.setLoLimit1(loLimit1);
                    property.setHiLimit2(hiLimit2);
                    property.setLoLimit2(loLimit2);
                    property.setHiLimit3(hiLimit3);
                    property.setLoLimit3(loLimit3);
                    property.setStander(stander);
                    property.setPercision(percision);
                    property.setSaved(saved3);
                    property.setUnit(unit);
                    break;

                /**
                 * DO = 4    数字输出量
                 * Type	          EnumType 	数据的类型
                 * ID	          long 	数据标识ID
                 * ParentID	      long 	父关系的ID
                 * Name	Char      [NAMELENGTH]	名字
                 * Desc	Char      [DES_LENGTH]	描述
                 * ControlEnable  EnumEnable 	可否控制标记
                 * Desc0	      Char [DES_LENGTH]	数字值为0时的描述
                 * Desc1	      Char [DES_LENGTH]	数字值为0时的描述
                 * Saved	      EnumEnable 	是否保存
                 * */
                case DataConst.DO:
                    int alarmEnable4 = contentBuf.readInt();
                    String des04 = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim();// 数据描述
                    String des14 = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim();// 数据描述
                    int saved4 = contentBuf.readInt();
                    property.setAlarmEnable(alarmEnable4);
                    property.setDesc0(des04);
                    property.setDesc0(des14);
                    property.setSaved(saved4);
                    break;

                /**
                 * AO = 5    模拟输出量
                 * MaxVal	float  	有效上限
                 * MinVal	float  	有效下限
                 * Alarmlevel	EnumAlarmLevel	告警等级
                 * AlarmEnable	EnumEnable	告警使能标记
                 * ControlEnable	EnumEnable 	可否控制标记
                 * HiLimit1	float  	一级告警上限
                 * LoLimit1	float  	一级告警下限
                 * HiLimit2	Float  	二级告警上限
                 * LoLimit2	float  	二级告警下限
                 * HiLimit3	float  	三级告警上限
                 * LoLimit3	float 	三级告警下限
                 * Stander	float  	标称值
                 * Percision	float  	精度
                 * Saved	EnumEnable 	是否保存
                 * Unit	char [UNIT_LENGTH]	单位
                 * */
                case DataConst.AO:
                    float maxVal5 = contentBuf.readFloat();
                    float minVal5 = contentBuf.readFloat();
                    int alarmlevel5 = contentBuf.readInt();
                    int alarmEnable5 = contentBuf.readInt();
                    int controlEnable = contentBuf.readInt();
                    float hiLimit15 = contentBuf.readFloat();
                    float loLimit15 = contentBuf.readFloat();
                    float hiLimit25 = contentBuf.readFloat();
                    float loLimit25 = contentBuf.readFloat();
                    float hiLimit35 = contentBuf.readFloat();
                    float loLimit35 = contentBuf.readFloat();
                    float stander5 = contentBuf.readFloat();
                    float percision5 = contentBuf.readFloat();
                    int saved5 = contentBuf.readInt();
                    String unit5 = contentBuf.readCharSequence(8, Charset.forName("GBK")).toString().trim(); //设备版本

                    property.setMaxVal(maxVal5);
                    property.setMinVal(minVal5);
                    property.setAlarmlevel(alarmlevel5);
                    property.setAlarmEnable(alarmEnable5);
                    property.setControlEnable(controlEnable);
                    property.setHiLimit1(hiLimit15);
                    property.setLoLimit1(loLimit15);
                    property.setHiLimit2(hiLimit25);
                    property.setLoLimit2(loLimit25);
                    property.setHiLimit3(hiLimit35);
                    property.setLoLimit3(loLimit35);
                    property.setStander(stander5);
                    property.setPercision(percision5);
                    property.setSaved(saved5);
                    property.setUnit(unit5);
                    break;

                /**
                 * STRIN = 6  字符串量
                 * AlarmEnable	EnumEnable 	告警使能标记
                 * Saved	EnumEnable	是否保存
                 * */
                case DataConst.STRIN:
                    int alarmEnable6 = contentBuf.readInt();
                    int saved6 = contentBuf.readInt();
                    property.setAlarmEnable(alarmEnable6);
                    property.setSaved(saved6);
                    break;

                default:
                    break;
            }

            properties.add(property);
        }
        baseInfo.setPropertyList(properties);
        return baseInfo;
    }

    private String getTimeStr(String time) {
        if (time.length() == 1) {
            return "0" + time;
        }
        return time;
    }

    /**
     * 解码之后的处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("开始处理属性数据：", JSON.toJSONString(obj)));
        DataBaseInfo baseInfo = (DataBaseInfo) obj;
        List<PropertyData> propertyList = baseInfo.getPropertyList();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //属性入库
        if (ObjectUtil.isNotNull(propertyList) && !propertyList.isEmpty()) {
            Gson gson = new Gson();

            for (PropertyData propertyData : propertyList) {
                try {
                    int type = propertyData.getType();
                    switch (type) {
                        //STATION = 0  局、站
                        case DataConst.STATION:
                            Station station = new Station();
                            BeanUtils.copyProperties(propertyData, station);
                            station.setId(propertyData.getPropertyId());
                            station.setName(parseDataId(propertyData.getPropertyId())+" "+propertyData.getName());
                            station.setStationId(propertyData.getPropertyId());
                            station.setCreateTime(baseInfo.getTime());
                            stationService.saveOrUpdate(station);
                            break;

                            //DEVICE = 1  设备
                        case DataConst.DEVICE:
                            Device device = new Device();
                            BeanUtils.copyProperties(propertyData, device);
                            device.setId(propertyData.getPropertyId());
                            device.setDeviceType(propertyData.getDeviceType());
                            device.setVersion(propertyData.getVersion());
                            device.setDeviceId(propertyData.getPropertyId());
                            device.setBeginRunTime(sdf.parse(propertyData.getBeginRunTime()));
                            device.setCreateTime(baseInfo.getTime());
                            deviceService.saveOrUpdate(device);
                            break;
/*
                            //DI = 2   二态数字输入量
                        case DataConst.DI:
                            String value2 = String.valueOf(contentBuf.readUnsignedByte()); //数值
                            int status2 = contentBuf.readInt();  //数值状态 EnumAlarmLevel
                            propertyValue.setValue(value2);
                            propertyValue.setStatus(status2);
                            break;

                        //AI = 3    模拟输入量
                        case DataConst.AI:
                            String value3 = String.valueOf(contentBuf.readFloat());
                            int status3 = contentBuf.readInt();  //数值状态 EnumAlarmLevel
                            propertyValue.setValue(value3);
                            propertyValue.setStatus(status3);
                            break;

                        //DO = 4    数字输出量
                        case DataConst.DO:
                            String value4 = String.valueOf(contentBuf.readUnsignedByte()); //数值
                            int status4 = contentBuf.readInt();  //数值状态 EnumAlarmLevel
                            propertyValue.setValue(value4);
                            propertyValue.setStatus(status4);
                            break;

                        //AO = 5    模拟输出量
                        case DataConst.AO:
                            String value5 = String.valueOf(contentBuf.readFloat());
                            int status5 = contentBuf.readInt();  //数值状态 EnumAlarmLevel
                            propertyValue.setValue(value5);
                            propertyValue.setStatus(status5);
                            break;

                        //STRIN = 6  字符串量
                        case DataConst.STRIN:
                            int len = contentBuf.readInt(); //字符串长度
                            String value6 = contentBuf.readCharSequence(len, Charset.forName("GBK")).toString(); //字符串值
                            propertyValue.setValue(value6);
                            break;*/

                        default:
                            Property property = new Property();
                            property.setId(propertyData.getPropertyId());
                            BeanUtils.copyProperties(propertyData,property);
                            propertyService.saveOrUpdate(property);
                            break;
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                }
                /*
                * 因暂时不要
                * */
              //  redisService.set(DataConst.DH_PROERTY + "_" + propertyData.getPropertyId(), gson.toJson(propertyData));
                redisService.set(DataConst.DH_PROERTY_PARENT + "_" + propertyData.getPropertyId(), propertyData.getParentID());
            }
        }
    }

    /**
     * 将 32 位数据ID 拆解为 AA.BBB.CC.DDD 格式的字符串
     * @return 形如 "1.83.0.0" 的字符串
     */
    private static String parseDataId(String idStr) {
        long id = Long.parseLong(idStr);
        long AA  = (id >> 27) & 0x1F;   // 高 5 位：CSC 内 LSC 编号
        long BBB = (id >> 17) & 0x3FF;  // 中间 10 位：LSC 内站点编号
        long CC  = (id >> 11) & 0x3F;   // 中间 6 位：站内监控对象编号
        long DDD = id & 0x7FF;          // 低 11 位：监控对象下属数据点编号

        return String.format("%d.%d.%d.%d", AA, BBB, CC, DDD);
    }
}
