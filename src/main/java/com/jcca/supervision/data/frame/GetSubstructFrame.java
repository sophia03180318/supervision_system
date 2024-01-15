package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;

import java.io.Serializable;
import java.util.Random;

/**
 * @description: 获取指定节点下层节点
 * @author: sophia
 * @create: 2023/11/23 09:41
 **/
public class GetSubstructFrame extends BaseDataFrame implements Serializable {

    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.GET_SUBSTRUCT;
    public static int LEN = DataConst.MIN_MSG_LEN + 4;
    private static String rootId;

    private GetSubstructFrame(String rootId) {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);
        this.setRootId(rootId);
    }


    public static void setRootId(String rootId) {
        GetSubstructFrame.rootId = rootId;
    }


    public static GetSubstructFrame newInstance(String rootId) {
        return new GetSubstructFrame(rootId);
    }
}