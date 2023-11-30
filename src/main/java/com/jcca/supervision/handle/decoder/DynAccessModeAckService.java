package com.jcca.supervision.handle.decoder;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.PropertyData;
import com.jcca.supervision.data.PropertyValue;
import com.jcca.supervision.entity.Property;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import com.jcca.supervision.service.PropertyService;
import com.jcca.util.MyIdUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sophia
 * @description 对实时数据进行解码
 * @date 2023/11/28 10:43
 */
@Service
public class DynAccessModeAckService implements ResponseHandleAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RedisService redisService;

    @Resource
    private PropertyService propertyService;


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.DYN_ACCESS_MODE_ACK;
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
        ArrayList<PropertyValue> propertyValues = new ArrayList<>();
        int groupId = contentBuf.readInt();//数据包序号
        int result = contentBuf.readInt();//是否设置成功
        if (result == DataConst.FAILURE) {
            logger.info(LogUtil.buildLog("实时数据设置失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return null;
        }
        int cnt = contentBuf.readInt();// 属性数量
        if (cnt == -1) {
            logger.info(LogUtil.buildLog("属性个数过多，不可实时获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }
        if (cnt == -2) {
            logger.info(LogUtil.buildLog("无指定ID，实时获取属性失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }

        for (int i = 0; i < cnt; i++) {
            PropertyValue propertyValue = new PropertyValue();
            int type = contentBuf.readInt();// 属性类型
            long propertyId = contentBuf.readUnsignedInt();// 数据ID
            propertyValue.setType(type);
            propertyValue.setPropertyId(String.valueOf(propertyId));
            switch (type) {
                //STATION = 0  局、站
                case DataConst.STATION:
                    break;

                //DEVICE = 1  设备
                case DataConst.DEVICE:
                    break;

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
                    break;

                default:
                    break;
            }
            propertyValues.add(propertyValue);
        }
        baseInfo.setPropertyValueList(propertyValues);
        return baseInfo;
    }

    /**
     * 解码之后的处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("处理属性数据：", JSON.toJSONString(obj)));
        DataBaseInfo baseInfo = (DataBaseInfo) obj;
        List<PropertyValue> propertyValues = baseInfo.getPropertyValueList();

        if (ObjectUtil.isNotNull(propertyValues) && !propertyValues.isEmpty()) {
            Gson gson = new Gson();
            for (PropertyValue value : propertyValues) {
                Object cacheData = redisService.get(DataConst.DH_PROERTY + "_" + value.getPropertyId());
                if (ObjectUtil.isNull(cacheData)) {
                    logger.error("未获取到任何监测数据点位：" + DataConst.DH_PROERTY + "_" + value.getPropertyId());
                    continue;
                }
                try {
                    String s = cacheData.toString();
                    PropertyData propertyData = gson.fromJson(s, PropertyData.class);
                    Property property = new Property();
                    BeanUtils.copyProperties(propertyData, property);
                    property.setId(MyIdUtil.getIncId());
                    property.setCreateTime(baseInfo.getTime());
                    property.setValue(value.getValue());
                    property.setStatus(value.getStatus());
                    propertyService.save(property);
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
    }
}
