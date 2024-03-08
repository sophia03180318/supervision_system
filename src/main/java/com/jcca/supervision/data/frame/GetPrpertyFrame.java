package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;

import java.io.Serializable;
import java.util.Random;

/**
 * @description: 获取指定节点的属性信息
 * @author: sophia
 * @create: 2023/11/23 09:41
 **/
public class GetPrpertyFrame extends BaseDataFrame implements Serializable {


    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.GET_PROPERTY;
    public static int LEN = DataConst.MIN_MSG_LEN + 4+4;
    private static String id;

    private GetPrpertyFrame(String rootId) {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);
        this.setId(rootId);
    }


    public static void setId(String id) {
        GetPrpertyFrame.id = id;
    }

    public static String getId() {
        return id;
    }

    public static GetPrpertyFrame newInstance(String id) {
        return new GetPrpertyFrame(id);
    }
}