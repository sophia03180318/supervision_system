package com.jcca.supervision.data;

import lombok.Data;

/**
 * @description: 节点信息
 * @author: sophia
 * @create: 2023/11/22 16:21
 **/
@Data
public class NodesData {
    private String Id;
    private String parentId;
    private String type;
}