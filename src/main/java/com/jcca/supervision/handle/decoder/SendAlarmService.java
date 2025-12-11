package com.jcca.supervision.handle.decoder;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.ItsmQueueReq;
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

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: 服务器主动发送告警
 * @author: sophia
 * @create: 2023/11/27 10:58
 **/
@Service
public class SendAlarmService implements ResponseHandleAdapter {
    @Resource
    private RedisService redisService;
    @Resource
    private AlarmService alarmService;
    String DH_QUEUE_KEY = "_broker_alarm_queue";
    int DH = 77; // 通信质量

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
        int cnt = contentBuf.readInt();// 告警数量
        if (cnt == -1) {
            logger.info(LogUtil.buildLog("告警信息过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return baseInfo;
        }
        if (cnt == -2) {
            logger.info(LogUtil.buildLog("无指定ID，获取告警失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return baseInfo;
        }

        // 校验是否有足够的字节用于 cnt 条告警
        if (contentBuf.readableBytes() < cnt * 168) {
            logger.error("严重错误：告警报文长度不足。声称有{}条，需要{}字节，实际仅{}字节。丢弃该包。",
                    cnt, cnt * 168, contentBuf.readableBytes());
            return baseInfo; // 或者丢弃/返回错误
        }

        ArrayList<Alarm> alarmDataList = new ArrayList<Alarm>();
        for (int j = 0; j < cnt; j++) {
            try {
                long dataId = contentBuf.readUnsignedInt();//数据ID
                int status = contentBuf.readInt();//状态
                String desc = contentBuf.readCharSequence(160, Charset.forName("GBK")).toString().trim(); //告警描述
                Alarm alarmData = new Alarm();
                alarmData.setPropertyId(String.valueOf(dataId));
                alarmData.setStatus(status);
                alarmData.setDescc(desc);
                alarmDataList.add(alarmData);
            } catch (Exception e) {
                logger.error("告警解析中途异常: " + e.toString());
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
        try {
            DataBaseInfo baseInfo = (DataBaseInfo) obj;
            if (ObjectUtil.isNotNull(baseInfo.getAlarmDataList()) && !baseInfo.getAlarmDataList().isEmpty()) {
                LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
                Date startOfTodayDate = Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant());
                List<Alarm> alarmDataList = baseInfo.getAlarmDataList();
                logger.info("接收告警数据：{}条", alarmDataList.size());
                List<Alarm> saveList = new ArrayList<>();
                for (Alarm alarm : alarmDataList) {
                    String parentId = getParentId(alarm.getPropertyId());
                    alarm.setCreateTime(baseInfo.getTime());
                    alarm.setDeviceId(parentId);
                    Alarm alarmInfo = decodeUtil.getAlarmInfo(alarm);
                    if(alarmInfo.getOccurrenceTime().after(startOfTodayDate)){//今天以前的数据不要
                        String incId = MyIdUtil.getIncId();
                        alarmInfo.setId(incId);
                        saveList.add(alarmInfo);
                        prepareAndSaveAlarm(alarm);
                    }
                }
                // 批量保存
                if (!saveList.isEmpty()) {
                    alarmService.saveBatch(saveList);
                }
            }
        } catch (Exception e) {
            logger.error("保存告警数据报错:" + e.toString());
        }
    }

    // 修正后的方法
    private String getParentId(String propertyId) {
        try {
            Object cacheData = redisService.get(DataConst.DH_PROERTY_PARENT + "_" + propertyId);
            if (ObjectUtil.isNotNull(cacheData)) {
                return (String) cacheData;
            }
            long id = Long.parseLong(propertyId);
            // 使用位掩码：保留高位，将低11位全部置0
            // 0xFFFFF800 的二进制表示：1111...1111100000000000 (末尾11个0)
            long parentId = id & ~0x7FF; // 或者 id & 0xFFFFFFFFFFFFF800L
            String result = String.valueOf(parentId);
            redisService.set(DataConst.DH_PROERTY_PARENT + "_" + propertyId, result);
            return result;
        } catch (Exception e) {
            return "0";
        }
    }

    private void prepareAndSaveAlarm(Alarm alarm) {
        ItsmQueueReq queueReq = new ItsmQueueReq();
        if ("开始".contains(alarm.getAlarmFlag())){
            queueReq.setAlarmState(0);
            queueReq.setAlarmContent("动环告警:"+alarm.getDescc());
        }else if("结束".contains(alarm.getAlarmFlag())||"取消".contains(alarm.getAlarmFlag())){
            queueReq.setAlarmState(1);
            queueReq.setAlarmContent("动环恢复:"+alarm.getDescc());
        }else{
            return;
        }
        queueReq.setCascoAlarmType(DH);
        queueReq.setAssetId(alarm.getDeviceId());
        queueReq.setEventId(alarm.getAlarmNumber());
        queueReq.setAlarmTime(alarm.getOccurrenceTime());
        redisService.convertAndSend(DH_QUEUE_KEY, JSONUtil.toJsonStr(queueReq));
    }

}