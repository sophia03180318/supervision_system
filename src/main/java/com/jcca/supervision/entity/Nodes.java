package com.jcca.supervision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: 节点表
 * @author: sophia
 * @create: 2023/12/23 16:53
 **/
@Data
@TableName("DH_NODE")
public class Nodes {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("PARENT_ID")
    private String parentId;
}