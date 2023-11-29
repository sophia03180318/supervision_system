package com.jcca.supervision.handle.decoder;

import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.data.DataNodes;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sophia
 * @description 响应节点之下 所有子孙节点ID
 * @date 2023/11/28 10:43
 */
@Service
public class SetNodesAckService implements ResponseHandleAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RedisService redisService;


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.SET_NODES;
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
        List<DataNodes> nodeList = new ArrayList<>();
        int nodeCount = contentBuf.readInt();// 节点个数
        if (nodeCount == -1) {
            logger.info(LogUtil.buildLog("节点个数过多，不可一次获取", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return nodeList;
        }
        if (nodeCount == -2) {
            logger.info(LogUtil.buildLog("无此节点，获取子节点失败", JSON.toJSONString(ByteBufUtil.hexDump(contentBuf))));
            return nodeList;
        }
        for (int i = 0; i < nodeCount; i++) {
            long nodeId = contentBuf.readUnsignedInt();
            long parentNodeId = contentBuf.readUnsignedInt();
            DataNodes node = new DataNodes();
            node.setId(Long.toString(nodeId));
            node.setParentId(Long.toString(parentNodeId));
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
        List<DataNodes> nodeList = baseInfo.getDataNodesList();
        String key = DataConst.DH_NODE;
        DataConst.NODE_SET.add(key);
        redisService.set(key, JSON.toJSONString(nodeList));
    }
}
