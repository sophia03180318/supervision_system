package com.jcca.util;

import java.nio.ByteBuffer;

/**
 * 字节相关管局
 */
public class ByteUtil {

    /**
     * int转小端存储
     *
     * @param a
     * @param len
     * @return
     */
    public static byte[] num2LittleEndian(int a, int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) (a >> (8 * i) & 0xFF);
        }

        return data;
    }

    /**
     * int转大端存储
     *
     * @param a
     * @param len
     * @return
     */
    public static byte[] num2BigEndian(int a, int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[len - i - 1] = (byte) (a >> (8 * i) & 0xFF);
        }

        return data;
    }


    /**
     * 用于建立十六进制字符的输出的大写字符数组
     */
    private static final char[] toDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String encodeHex(byte[] data) {
        final int len = data.length;
        final char[] out = new char[3 * len];//len*3
        // two characters from the hex value.
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];// 高位
            out[j++] = toDigits[0x0F & data[i]];// 低位
            out[j++] = 32; //添加空格


        }
        return new String(out);
    }

    /**
     * ByteBuffe转ByteArray
     *
     * @return
     */
    public static byte[] ByteBuffe2ByteArray(ByteBuffer buffer) {
        buffer.flip();
        byte[] array = new byte[buffer.remaining()];
        buffer.get(array, 0, array.length);
        return array;
    }


    public static String getDhId(long id) {
        String str = new StringBuilder(Long.toBinaryString(id)).reverse().toString();

       String ddd = Integer.toHexString(Integer.parseInt(str.substring(0, 11),2));
        String cc = Integer.toHexString(Integer.parseInt(str.substring(11, 17),2));
        String bbb = Integer.toHexString(Integer.parseInt(str.substring(17, 27),2));
        String aa = Integer.toHexString(Integer.parseInt(str.substring(27),2));

/*        String aa = Integer.toHexString(Integer.parseInt(string.substring(0,5),2));
        String bbb = Integer.toHexString(Integer.parseInt(string.substring(5, 15),2));
        String cc = Integer.toHexString(Integer.parseInt(string.substring(15, 21),2));
        String ddd = Integer.toHexString(Integer.parseInt(string.substring(21),2));*/
        return aa+"."+bbb+"."+cc+"."+ddd;
    }


}
