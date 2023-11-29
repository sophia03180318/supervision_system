package com.jcca.supervision.handle.decoder;

import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.controller.TcpController;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author sophia
 * @description 登录出确认
 * @date 2023/11/28 10:43
 */
@Service
public class LogoutAckService implements ResponseHandleAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.LOGOUT_ACK;
    }

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    @Override
    public Object decode(ByteBuf contentBuf) {

        //登出结果
        int result = contentBuf.readInt();
        if (result == DataConst.FAILURE) {
            logger.info(LogUtil.buildLog("登出失败：登出结果为", result));
        }
        if (result == DataConst.SUCCESS) {
            //关闭TCP二号链接
            TcpController.stopTcp2();
            //关闭TCP一号链接
            TcpController.stopTcp1();
        }
        return null;
    }

    /**
     * 接收到解码后的数据 并进行处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("登出业务处理：", JSON.toJSONString(obj)));
    }

}
