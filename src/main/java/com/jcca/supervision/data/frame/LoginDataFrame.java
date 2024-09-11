package com.jcca.supervision.data.frame;

import com.jcca.common.config.TcpConfig;
import com.jcca.supervision.constant.DataConst;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Random;

/**
 * @description: 登录报文
 * @author: sophia
 * @create: 2023/11/23 09:41
 **/
public class LoginDataFrame extends BaseDataFrame implements Serializable {

    @Resource
    private TcpConfig tcpConfig;

    /**
     * 命令字
     */
    public static int DATA_TYPE = DataConst.LOGIN;
    public static int LEN = DataConst.MIN_MSG_LEN + 60;
    public static String USER_NAME = "zhjc";
    public static String PASSWORD = "123";

    private LoginDataFrame() {
        this.setLen(LEN);
        this.setNum(new Random().nextInt());
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