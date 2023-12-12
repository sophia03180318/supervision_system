package com.jcca.supervision.handle.decoder.utils;

import com.jcca.supervision.entity.Alarm;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @description: 解码工具包
 * @author: sophia
 * @create: 2023/11/27 16:00
 **/
public class decodeUtil {
    public String getIDStr(long idl) {
        String s = Long.toBinaryString(idl);
        //长度小于32
        String idStr = "";
        for (int i = s.length(); i <= 32; i++) {
            idStr += "0";
        }
        idStr += s;
        String id = idStr.substring(0, 32);
        return id;
    }

    public static Alarm getAlarmInfo(Alarm alarm) {
        String desc = alarm.getDesc();
        String[] split = desc.split("\\t");
        for (int i = 0; i < split.length; i++) {
            String data = split[i].trim();
            if (i == 0) {
                alarm.setAlarmId(data.replace("[",""));
                continue;
            }
            if (i == 1) {
                alarm.setName(data);
                continue;
            }
            if (i == 2) {
                try {
                    alarm.setOccurrenceTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data));
                } catch (ParseException e) {
                }
                continue;
            }
            if (i == 3) {
                alarm.setIdDesc(data);
                continue;
            }
            if (i == 4) {
                alarm.setLevelStr(data);
                continue;
            }
            if (i == 5) {
                alarm.setAlarmNumber(data);
                continue;
            }
            if (i == 6) {
                alarm.setAlarmFlag(data);
                continue;
            }
            if (i == 7) {
                String[] split1 = data.split("]");
                alarm.setAlarmInfo(split1[0].trim());
                continue;
            }
        }
        return alarm;
    }

}