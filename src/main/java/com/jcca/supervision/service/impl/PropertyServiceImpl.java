package com.jcca.supervision.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.supervision.mapper.PropertyMapper;
import com.jcca.supervision.entity.Property;
import com.jcca.supervision.service.PropertyService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:10
 **/
@Service
public class PropertyServiceImpl extends ServiceImpl<PropertyMapper, Property> implements PropertyService {
}