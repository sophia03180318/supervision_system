package com.jcca.supervision.data;

import com.jcca.supervision.entity.Alarm;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author sophia
 * @description 基础信息
 * @date 2023/11/27 9:23
 */
@Data
public class DataBaseInfo {

    //数据类型
    private Integer code;

    //数据解码时间
    private Date time;

    //数据包序号
    private long num;

    //节点数据
    private List<Nodes> dataNodesList;

    //属性
    private List<PropertyData> propertyList;

    //告警数据
    private List<Alarm> alarmDataList;

    //监测值
    private List<PropertyValue> propertyValueList;


}
