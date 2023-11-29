package com.jcca.supervision.data.frame;

import com.jcca.supervision.constant.DataConst;

import java.io.Serializable;

/**
 * @description: 登录报文
 * @author: sophia
 * @create: 2023/11/23 09:41
 **/
public class LoginDataFrame extends BaseDataFrame implements Serializable {

    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.LOGIN;
    public static int LEN = DataConst.MIN_MSG_LEN + 60;
    public static String USER_NAME = DataConst.USER_NAME;
    public static String PASSWORD = DataConst.PASSWORD;

    private LoginDataFrame() {
        this.setLen(LEN);
        this.setNum(1);
        this.setType(DATA_TYPE);
    }
    /**
     * 用户名  40
     * 不够40字节的以空格补齐
     */
    private String userName;

    /**
     * 口令  20
     * 不够20字节的以空格补齐
     */
    private String password;


    //将名称补至 40字节
    public static  String getUserName() {
        String name = USER_NAME;
        while (name.getBytes().length < 40) {
            name += " ";
        }
        return name;
    }

    //将口令补至 20字节
    public static String getPassword() {
        String password = PASSWORD;
        while (password.getBytes().length < 20) {
            password += " ";
        }
        return password;
    }


    public static class SingletonHolder {
        public static LoginDataFrame instance = new LoginDataFrame();
    }

    public static LoginDataFrame newInstance() {
        return LoginDataFrame.SingletonHolder.instance;
    }
}