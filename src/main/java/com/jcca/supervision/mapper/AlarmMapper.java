package com.jcca.supervision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.supervision.entity.Alarm;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

/**
 * @description: 告警
 * @author: sophia
 * @create: 2023/11/30 15:26
 **/
public interface AlarmMapper extends BaseMapper<Alarm> {
    @Delete("delete from DH_ALARM")
    void deleteAlarm();

    @Update("update ALARM_INFO set ALARM_STATE= 2 , STATUS =2 WHERE  EVENT_CATEGORY='event:event_environment' and (ALARM_STATE= 1 or STATUS =1)")
    void recoverAlarm();

}