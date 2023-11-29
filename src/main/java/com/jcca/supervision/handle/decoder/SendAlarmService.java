package com.jcca.supervision.handle.decoder;

import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.config.TcpConfig;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.AlarmData;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @description: 服务器主动发送告警
 * @author: sophia
 * @create: 2023/11/27 10:58
 **/
@Service
public class SendAlarmService implements ResponseHandleAdapter {
    @Resource
    private TcpConfig tcpConfig;

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.SEND_ALARM;
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
        ArrayList<AlarmData> alarmDataList = new ArrayList<AlarmData>();
        int cnt = contentBuf.readInt();// 告警数量
        if (cnt == -1) {
            logger.info(LogUtil.buildLog("告警信息过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }
        if (cnt == -2) {
            logger.info(LogUtil.buildLog("无指定ID，获取告警失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }
        for (int i = 0; i < cnt; i++) {
            long dataId = contentBuf.readUnsignedInt();//数据ID
            int level = contentBuf.readInt();//状态
            //告警等级不够直接掠过
            if (level == DataConst.OPEVENT || level == DataConst.NOALARM || level == DataConst.INVALID2) {
                continue;
            }
            String desc = contentBuf.readCharSequence(160, CharsetUtil.US_ASCII).toString().trim(); //告警描述
            AlarmData alarmData = new AlarmData();
            alarmData.setDataId(String.valueOf(dataId));
            alarmData.setLevel(level);
            alarmData.setDesc(desc);
            alarmDataList.add(alarmData);
        }
        baseInfo.setAlarmDataList(alarmDataList);
        return alarmDataList;
    }

    /**
     * 接收到解码后的数据 并进行处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("登录业务处理：", JSON.toJSONString(obj)));
    }

}