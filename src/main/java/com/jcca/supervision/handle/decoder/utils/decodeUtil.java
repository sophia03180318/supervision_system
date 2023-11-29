package com.jcca.supervision.handle.decoder.utils;

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

}