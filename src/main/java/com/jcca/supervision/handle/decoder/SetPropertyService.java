package com.jcca.supervision.handle.decoder;

import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.DataNodes;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sophia
 * @description 获取指定ID的属性值
 * @date 2023/11/28 10:43
 */
@Service
public class SetPropertyService implements ResponseHandleAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RedisService redisService;


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
        int cnt = contentBuf.readInt();// 属性数量
        if (cnt == -1) {
            logger.info(LogUtil.buildLog("属性数量过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }
        if (cnt == -2) {
            logger.info(LogUtil.buildLog("无指定ID，获取属性失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }

        for (int i = 0; i < cnt; i++) {
            int type = contentBuf.readInt();// 属性类型
            long dateId = contentBuf.readUnsignedInt();// 数据ID
            long deviceId = contentBuf.readUnsignedInt();// 设备ID
            String name = contentBuf.readCharSequence(40, CharsetUtil.US_ASCII).toString().trim(); //名称
            String des = contentBuf.readCharSequence(160, CharsetUtil.US_ASCII).toString().trim();// 数据描述

            switch (type) {
                //STATION = 0  局、站
                case DataConst.STATION:
                    float longitude = contentBuf.readFloat(); //经度
                    float latitude = contentBuf.readFloat(); //纬度
                    break;

                //DEVICE = 1  设备
                case DataConst.DEVICE:
                    int deviceType = contentBuf.readInt();
                    String deviceManufacturer = contentBuf.readCharSequence(40, CharsetUtil.US_ASCII).toString().trim(); //生产厂家
                    String deviceVersion = contentBuf.readCharSequence(40, CharsetUtil.US_ASCII).toString().trim(); //设备版本
                    //上架时间
                    short years = contentBuf.readShort();
                    char month = contentBuf.readChar();
                    char day = contentBuf.readChar();
                    char hour = contentBuf.readChar();
                    char minute = contentBuf.readChar();
                    char second = contentBuf.readChar();
                    String beginRunTime = String.valueOf(years) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                    break;
                //DI = 2   二态数字输入量
                case DataConst.DI:
                    //暂不做处理
                    break;

                //AI = 3    模拟输入量
                case DataConst.AI:
                    //暂不做处理
                    break;

                //DO = 4    数字输出量
                case DataConst.DO:
                    //暂不做处理
                    break;

                //AO = 5    模拟输出量
                case DataConst.AO:
                    //暂不做处理
                    break;

                //STRIN = 6  字符串量
                case DataConst.STRIN:
                    //暂不做处理
                    break;

                default:
                    break;
            }
        }
        return baseInfo;
    }

    /**
     * 解码之后的处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("开始处理节点数据：", JSON.toJSONString(obj)));
        DataBaseInfo baseInfo = (DataBaseInfo) obj;
        List<DataNodes> nodeList = baseInfo.getDataNodesList();
        String key = DataConst.DH_NODE;
        DataConst.NODE_SET.add(key);
        redisService.set(key, JSON.toJSONString(nodeList));
    }
}
