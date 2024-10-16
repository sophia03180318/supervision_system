package com.jcca.supervision.handle.decoder;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.entity.Alarm;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import com.jcca.supervision.handle.decoder.utils.decodeUtil;
import com.jcca.supervision.service.AlarmService;
import com.jcca.util.MyIdUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 当前告警
 * @author: sophia
 * @create: 2023/11/27 10:58
 **/
@Service
public class SetActiveAlarmService implements ResponseHandleAdapter {
    @Resource
    private RedisService redisService;
    @Resource
    private AlarmService alarmService;

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.SET_ACTIVE_ALARM;
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
        ArrayList<Alarm> alarmDataList = new ArrayList<Alarm>();
        int cnt = contentBuf.readInt();// 告警数量
        if (cnt == -1) {
            logger.info(LogUtil.buildLog("当前告警信息过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
        }
        logger.info("获取到" + cnt + "条告警~");
        for (int i = 0; i < cnt; i++) {
            try {
                long dataId = contentBuf.readUnsignedInt();//数据ID
                int level = contentBuf.readInt();//状态
                //告警等级不够直接掠过
                if (level == DataConst.OPEVENT || level == DataConst.NOALARM || level == DataConst.INVALID2) {
                    continue;
                }
                String desc = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim(); //告警描述
                Alarm alarmData = new Alarm();
                alarmData.setPropertyId(String.valueOf(dataId));
                alarmData.setLevell(level);
                //解析告警详情
                alarmData.setDescc(desc);
                alarmDataList.add(alarmData);
            } catch (Exception e) {
                logger.info("获取到" + cnt + "条告警,但解析中发生错误");
            }
        }
        baseInfo.setAlarmDataList(alarmDataList);
        return baseInfo;
    }

    /**
     * 接收到解码后的数据 并进行处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("告警信息处理：", JSON.toJSONString(obj)));
        DataBaseInfo baseInfo = (DataBaseInfo) obj;
        if (ObjectUtil.isNotNull(baseInfo.getAlarmDataList()) && !baseInfo.getAlarmDataList().isEmpty()) {
            List<Alarm> alarmDataList = baseInfo.getAlarmDataList();
            for (Alarm alarm : alarmDataList) {
                Object cacheData = redisService.get(DataConst.DH_PROERTY_PARENT + "_" + alarm.getPropertyId());
                if (ObjectUtil.isNull(cacheData)) {
                    logger.error("未获取到此监测点位的设备ID：" + DataConst.DH_PROERTY_PARENT + "_" + alarm.getPropertyId());
                    continue;
                }
                alarm.setCreateTime(baseInfo.getTime());
                alarm.setDeviceId((String) cacheData);
                Alarm alarmInfo = decodeUtil.getAlarmInfo(alarm);
                if (StringUtils.isEmpty(alarmInfo.getAlarmId())) {
                    String incId = MyIdUtil.getIncId();
                    alarmInfo.setId(incId);
                    alarmInfo.setAlarmId(incId);
                } else {
                    alarmInfo.setId(alarm.getAlarmId());
                }
                alarmService.saveOrUpdate(alarmInfo);
            }
        }
    }
}