package com.jcca.common;

import com.alibaba.fastjson.JSON;
import com.jcca.util.MyIdUtil;

/**
 * @Description 日志工具类
 * @ClassName LogUtil
 * @Date 2022/4/20 17:03
 * @Author hanwone
 * @Since 2.0.0.1
 */
public class LogUtil {


    /**
     * @param host   主机IP端口
     * @param action 操作说明
     * @param data   需要打印的数据
     * @return 日志内容
     */
    public static String buildLog(String host, String action, Object data) {
        host = host == null ? "本机" : host;
        return "[" + MyIdUtil.getIncId() + "]-[" + host + "]-[" + action + "]-[" + JSON.toJSONString(data) + "]";
    }

    /**
     * @param action 操作说明
     * @param data   需要打印的数据
     * @return 日志内容
     */
    public static String buildLog(String action, Object data) {
        return "[" + MyIdUtil.getIncId() + "]-[" + action + "]-[" + JSON.toJSONString(data) + "]";
    }
}
