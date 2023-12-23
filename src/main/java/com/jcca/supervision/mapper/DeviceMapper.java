package com.jcca.supervision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.supervision.entity.Device;
import org.apache.ibatis.annotations.Select;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:02
 **/
public interface DeviceMapper extends BaseMapper<Device> {
    @Select("delete from DH_DEVICE")
    void removeAll();
}