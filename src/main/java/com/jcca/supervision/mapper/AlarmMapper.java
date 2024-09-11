package com.jcca.supervision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.supervision.entity.Alarm;
import org.apache.ibatis.annotations.Delete;

/**
 * @description: 告警
 * @author: sophia
 * @create: 2023/11/30 15:26
 **/
public interface AlarmMapper extends BaseMapper<Alarm> {
    @Delete("delete  from DH_ALARM")
    void deleteAlarm();
}