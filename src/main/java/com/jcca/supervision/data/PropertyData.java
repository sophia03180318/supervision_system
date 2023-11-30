package com.jcca.supervision.data;

import lombok.Data;

/**
 * @description: 属性信息
 * @author: sophia
 * @create: 2023/11/29 17:26
 *
 **/
@Data
public class PropertyData{

    /**
     * AI = 3    模拟输入量
     * MaxVal	float  	有效上限
     * MinVal	float  	有效下限
     * Alarmlevel	EnumAlarmLevel	告警等级
     * AlarmEnable	EnumEnable	告警使能标记
     * ControlEnable	EnumEnable 	可否控制标记
     * AlarmThresbhold	EnumEnable	告警触发阀值
     * HiLimit1	float  	一级告警上限
     * LoLimit1	float  	一级告警下限
     * HiLimit2	float  	二级告警上限
     * LoLimit2	float  	二级告警下限
     * HiLimit3	float  	三级告警上限
     * LoLimit3	float 	三级告警下限
     * Stander	float  	标称值
     * Percision	float  	精度
     * Saved	EnumEnable 	是否保存历史
     * Unit	char [UNIT_LENGTH]	单位
     * Desc0	Char [DES_LENGTH]	数字值为0时的描述
     * Desc1	Char [DES_LENGTH]	数字值为0时的描述
     * */
    private int type;
    private String propertyId;
    private String parentID;
    private String name;
    private String desc;
    private Float maxVal;
    private Float minVal;
    private Integer alarmlevel;
    private Integer alarmEnable;
    private Integer controlEnable;
    private Integer alarmThresbhold;
    private Float hiLimit1;
    private Float loLimit1;
    private Float hiLimit2;
    private Float loLimit2;
    private Float hiLimit3;
    private Float loLimit3;
    private float stander;
    private float percision;
    private Integer saved;
    private String unit;
    private String desc0;
    private String desc1;


    private Integer deviceType; //设备类型
    private String productor;   //生产厂家
    private String version;     //设备版本
    private String beginRunTime; //上架时间
    private Float longitude; //经度
    private Float latitude; //纬度




}