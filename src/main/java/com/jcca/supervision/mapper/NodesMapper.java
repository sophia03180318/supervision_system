package com.jcca.supervision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.supervision.entity.Nodes;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 节点
 * @author: sophia
 * @create: 2023/11/30 15:26
 **/
public interface NodesMapper extends BaseMapper<Nodes> {
    @Select("select id from DH_NODE where PARENT_ID = #{nodeId}")
    List<String> getNodesByParentId(String nodeId);
}