package com.jcca.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * 时间工具
 */
@Slf4j
public class MyDateUtil {
    /**
     * ldt时间获取毫秒数
     *
     * @param ldt
     * @return
     */
    public static String ltdToMillisecond(LocalDateTime ldt) {
        //获取毫秒数
        Long milliSecond = ldt.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return milliSecond.toString();
    }

    /**
     * 毫秒数转时间
     *
     * @param ms
     * @return
     */
    public static DateTime msToDateTime(String ms) {
        DateTime date = DateUtil.date(Convert.toLong(ms));
        return date;
    }

    ;

    /**
     * 输出执行时间
     *
     * @param name
     * @param starttime
     */
    public static String execTime(String name, Long starttime) {
        Double time = Convert.toDouble(DateUtil.spendMs(starttime));
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":").append(time).append(",指定时间毫秒,").append(NumberUtil.decimalFormat("#.##", time / 1000))
                .append("秒,").append(NumberUtil.decimalFormat("#.##", time / 1000 / 60)).append("分钟");
        return sb.toString();
    }

    public static String getNowStr() {
        String str = DateUtil.format(new Date(), "yyyy-MM-dd-HH:mm:ss");
        return str;
    }

}
