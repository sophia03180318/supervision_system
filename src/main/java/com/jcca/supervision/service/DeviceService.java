package com.jcca.supervision.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.supervision.entity.Device;

/**
 * @description: 设备
 * @author: sophia
 * @create: 2023/11/30 14:00
 **/
public interface DeviceService extends IService<Device> {
    /**
     * 清空设备数据
     * */
    void removeAll();


}