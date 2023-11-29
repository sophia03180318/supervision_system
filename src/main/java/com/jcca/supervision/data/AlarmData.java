package com.jcca.supervision.data;

import lombok.Data;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/27 11:40
 **/
@Data
public class AlarmData {

    //数据ID
    private String dataId;

    //告警等级
    private Integer level;

    //告警描述
    private String desc;
}