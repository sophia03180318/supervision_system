package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;
import lombok.Data;

import java.io.Serializable;
import java.util.Random;

/**
 * @author sophia
 * @description 获取当前告警
 * @date 2023/12/6 10:15
 */
@Data
public class GetActiveAlarmFrame extends BaseDataFrame implements Serializable {


    //当前告警
    public static int DATA_TYPE = DataConst.GET_ACTIVE_ALARM;
    public static int LEN = DataConst.MIN_MSG_LEN;

    private GetActiveAlarmFrame() {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);

    }

    public static class SingletonHolder {
        public static GetActiveAlarmFrame instance = new GetActiveAlarmFrame();
    }

    public static GetActiveAlarmFrame newInstance() {
        return GetActiveAlarmFrame.SingletonHolder.instance;
    }
}
