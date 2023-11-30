package com.jcca.supervision.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.supervision.entity.Alarm;
import com.jcca.supervision.mapper.AlarmMapper;
import com.jcca.supervision.service.AlarmService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 15:28
 **/
@Service
public class AlarmServiceImpl extends ServiceImpl<AlarmMapper, Alarm> implements AlarmService {
}