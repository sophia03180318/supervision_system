package com.jcca.supervision.data;

import lombok.Data;

/**
 * @description: 监测数值
 * @author: sophia
 * @create: 2023/11/30 16:41
 **/
@Data
public class PropertyValue {
    private int type;
    private String propertyId;
    private String value;
    private int status;
}