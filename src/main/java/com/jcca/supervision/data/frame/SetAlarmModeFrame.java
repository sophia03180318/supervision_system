package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;

import java.io.Serializable;
import java.util.Random;

/**
 * @description: 告警数据的方式设置
 * @author: sophia
 * @create: 2023/12/23 09:41
 **/
public class SetAlarmModeFrame extends BaseDataFrame implements Serializable {

    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.SET_ALARM_MODE;
    public static int LEN = DataConst.MIN_MSG_LEN + 12;
    private static String groupId;
    private static String mode;
    private static String count;
    private static String ids;

    private SetAlarmModeFrame(String mode) {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
        this.setType(DATA_TYPE);
        this.setGroupId("123321");
        this.setMode(mode);
        //this.setCount("0");
       // this.setIds("1");
    }


    public static void setGroupId(String groupId) {
        SetAlarmModeFrame.groupId = groupId;
    }
    public static void setMode(String mode) {
        SetAlarmModeFrame.mode = mode;
    }
    public static void setCount(String count) {
        SetAlarmModeFrame.count = count;
    }
    public static void setIds(String ids) {
        SetAlarmModeFrame.ids = ids;
    }

    public static String getGroupId() {
        return groupId;
    }

    public static String getMode() {
        return mode;
    }

    public static String getCount() {
        return count;
    }

    public static String getIds() {
        return ids;
    }

    public static SetAlarmModeFrame newInstance(String rootId) {
        return new SetAlarmModeFrame(rootId);
    }
}