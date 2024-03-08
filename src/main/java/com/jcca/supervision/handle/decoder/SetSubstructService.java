package com.jcca.supervision.handle.decoder;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.NodesData;
import com.jcca.supervision.entity.Nodes;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import com.jcca.supervision.service.NodesService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sophia
 * @description 响应节点下一层子节点ID
 * @date 2023/11/28 10:43
 */
@Service
public class SetSubstructService implements ResponseHandleAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RedisService redisService;
    @Resource
    private NodesService nodesService;

    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.SET_SUBSTRUCT;
    }

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    @Override
    public Object decode(ByteBuf contentBuf) {
        DataBaseInfo baseInfo = new DataBaseInfo();
        List<NodesData> nodeList = new ArrayList<>();
        int nodeCount = contentBuf.readInt();// 节点个数
        if (nodeCount == 0) {
            return nodeList;
        }
        if (nodeCount == -1) {
            logger.info(LogUtil.buildLog("节点个数过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return nodeList;
        }
        if (nodeCount == -2) {
            logger.info(LogUtil.buildLog("无此节点，获取子节点失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return nodeList;
        }
        Object o = redisService.get(DataConst.DH_NODE_ID);
        String parentId = "0";
        if (ObjectUtil.isNotNull(o)) {
            parentId = (String) o;
        }
        for (int i = 0; i < nodeCount; i++) {
            long nodeId = contentBuf.readUnsignedInt();
            NodesData node = new NodesData();
            node.setId(Long.toString(nodeId));
            node.setParentId(parentId);
            nodeList.add(node);
        }
        baseInfo.setDataNodesList(nodeList);
        return baseInfo;
    }

    /**
     * 解码之后的处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("开始处理节点数据：", JSON.toJSONString(obj)));
        DataBaseInfo baseInfo = (DataBaseInfo) obj;
        List<NodesData> nodeList = baseInfo.getDataNodesList();
        for (NodesData nodesData : nodeList) {
            Nodes nodes = new Nodes();
            nodes.setId(nodesData.getId());
            nodes.setParentId(nodesData.getParentId());
            nodes.setCreateTime(new Date());
            nodesService.saveOrUpdate(nodes);
        }
        redisService.set(DataConst.DH_NODE_ID_LIST, nodeList.stream().map(NodesData::getId).collect(Collectors.toList()));
    }
}
