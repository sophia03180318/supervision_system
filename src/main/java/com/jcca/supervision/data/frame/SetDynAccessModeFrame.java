package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;

import java.io.Serializable;
import java.util.Random;

/**
 * @description: 设置实时数据获取方式
 * @author: sophia
 * @create: 2023/12/23 09:41
 **/
public class SetDynAccessModeFrame extends BaseDataFrame implements Serializable {

    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.GET_NODES;
    public static int LEN = DataConst.MIN_MSG_LEN + 4;
    private static Integer seconds;

    private SetDynAccessModeFrame(Integer seconds) {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);
        this.setSeconds(seconds);
    }


    public static void setSeconds(Integer seconds) {
        SetDynAccessModeFrame.seconds = seconds;
    }


    public static SetDynAccessModeFrame newInstance(Integer seconds) {
        return new SetDynAccessModeFrame(seconds);
    }
}