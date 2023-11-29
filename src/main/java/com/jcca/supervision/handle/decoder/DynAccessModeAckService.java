package com.jcca.supervision.handle.decoder;

import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
            int type = contentBuf.readInt();// 属性类型
            long dateId = contentBuf.readUnsignedInt();// 数据ID

            switch (type) {
                //STATION = 0  局、站
                case DataConst.STATION:
                    break;

                //DEVICE = 1  设备
                case DataConst.DEVICE:
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
                    char value = contentBuf.readChar(); //数值
                    int status = contentBuf.readInt();  //数值状态 EnumAlarmLevel
                    break;

                //AO = 5    模拟输出量
                case DataConst.AO:
                    Float valueFloat = contentBuf.readFloat();
                    int status2 = contentBuf.readInt();  //数值状态 EnumAlarmLevel
                    break;

                //STRIN = 6  字符串量
                case DataConst.STRIN:
                    int len = contentBuf.readInt(); //字符串长度
                    String valueStr = contentBuf.readCharSequence(len, CharsetUtil.US_ASCII).toString(); //字符串值
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
        logger.info(LogUtil.buildLog("处理属性数据：", JSON.toJSONString(obj)));

    }
}
