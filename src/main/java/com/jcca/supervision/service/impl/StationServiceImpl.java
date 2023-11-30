package com.jcca.supervision.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.supervision.mapper.StationMapper;
import com.jcca.supervision.entity.Station;
import com.jcca.supervision.service.StationService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:04
 **/
@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {
}