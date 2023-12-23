package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;
import lombok.Data;

import java.io.Serializable;
import java.util.Random;

/**
 * @author sophia
 * @description 心跳数据包
 * @date 2023/12/6 10:15
 */
@Data
public class HeartbeatFrame extends BaseDataFrame implements Serializable {


    // 心跳数据类型
    public static int DATA_TYPE = DataConst.HEART_BEAT;
    public static int LEN = DataConst.MIN_MSG_LEN;

    private HeartbeatFrame() {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);

    }

    public static class SingletonHolder {
        public static HeartbeatFrame instance = new HeartbeatFrame();
    }

    public static HeartbeatFrame newInstance() {
        return HeartbeatFrame.SingletonHolder.instance;
    }
}
