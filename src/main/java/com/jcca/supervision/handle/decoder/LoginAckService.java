package com.jcca.supervision.handle.decoder;

import com.alibaba.fastjson.JSON;
import com.jcca.common.LogUtil;
import com.jcca.common.config.TcpConfig;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author sophia
 * @description 处理登录确认
 * @date 2023/11/28 10:43
 */
@Service
public class LoginAckService implements ResponseHandleAdapter {
    @Resource
    private TcpConfig tcpConfig;

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.LOGIN_ACK;
    }

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    @Override
    public Object decode(ByteBuf contentBuf) {

        //登录权限
        int rightMode = contentBuf.readInt();
        logger.info(LogUtil.buildLog("登录响应，权限代码为" ,rightMode));
        if (rightMode==DataConst.INVALID){
            logger.info(LogUtil.buildLog("登录失失败，用此户无权限：权限代码为" ,rightMode));
        }
        if (rightMode==DataConst.LEVEL1){
            logger.info(LogUtil.buildLog("登录成功，获取读权限：权限代码为" ,rightMode));
        }
        if (rightMode==DataConst.LEVEL2){
            logger.info(LogUtil.buildLog("登录成功：获取读写权限：权限代码为" ,rightMode));
/*            // 登陆成功启动客户端
        Executor taskExecutor = (Executor) SpringUtil.getBean("taskExecutor");
        taskExecutor.execute(() -> {
           NettyTCPClient2 tcpClient2 = new NettyTCPClient2(tcpConfig.getTcpIp(), tcpConfig.getTcpPort2());
            tcpClient2.connect();
        });*/
        }

        DataBaseInfo baseInfo = new DataBaseInfo();



        return baseInfo;
    }

    /**
     * 接收到解码后的数据 并进行处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {
        logger.info(LogUtil.buildLog("登录业务处理：", JSON.toJSONString(obj)));
    }
}
