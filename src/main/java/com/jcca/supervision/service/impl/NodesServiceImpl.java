package com.jcca.supervision.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.supervision.entity.Nodes;
import com.jcca.supervision.mapper.NodesMapper;
import com.jcca.supervision.service.NodesService;
import org.springframework.stereotype.Service;

/**
 * @description: 节点
 * @author: sophia
 * @create: 2023/11/30 15:28
 **/
@Service
public class NodesServiceImpl extends ServiceImpl<NodesMapper, Nodes> implements NodesService {
}