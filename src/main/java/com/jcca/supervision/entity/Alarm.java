package com.jcca.supervision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/27 11:40
 **/
@Data
@TableName("DH_ALARM")
public class Alarm {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("PROPERTY_ID")
    private String propertyId;

    //告警等级
    @TableField("ALARM_LEVEL")
    private Integer level;

    //告警描述
    @TableField("ALARM_DESC")
    private String desc;


    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}