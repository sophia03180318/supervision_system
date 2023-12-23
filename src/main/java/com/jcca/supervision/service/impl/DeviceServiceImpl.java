package com.jcca.supervision.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.supervision.mapper.DeviceMapper;
import com.jcca.supervision.entity.Device;
import com.jcca.supervision.service.DeviceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:01
 **/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
    @Resource
    private DeviceMapper deviceMapper;

    @Override
    public void removeAll() {
        deviceMapper.removeAll();
    }
}