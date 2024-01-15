package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;

import java.io.Serializable;
import java.util.Random;

/**
 * @description: 获取指定节点下所有子孙节点
 * @author: sophia
 * @create: 2023/12/23 09:41
 **/
public class GetNodesFrame extends BaseDataFrame implements Serializable {

    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.GET_NODES;
    public static int LEN = DataConst.MIN_MSG_LEN + 4;
    private static String rootId;

    private GetNodesFrame(String rootId) {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);
        this.setRootId(rootId);
    }


    public static void setRootId(String rootId) {
        GetNodesFrame.rootId = rootId;
    }


    public static GetNodesFrame newInstance(String rootId) {
        return new GetNodesFrame(rootId);
    }
}