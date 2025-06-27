package com.jcca.supervision.handle.decoder;

import com.jcca.common.LogUtil;
import com.jcca.common.RedisService;
import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.DataBaseInfo;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import com.jcca.supervision.service.AlarmService;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: 服务返回设置结果
 * @author: sophia
 * @create: 2023/11/27 10:58
 **/
@Service
public class SetAlarmModeService implements ResponseHandleAdapter {
    @Resource
    private RedisService redisService;

    @Resource
    private AlarmService alarmService;

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.ALARM_MODE_ACK;
    }

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    @Override
    public Object decode(ByteBuf contentBuf) {
        DataBaseInfo baseInfo = new DataBaseInfo();
        //登录权限
        long groupId = contentBuf.readUnsignedInt();
        int result = contentBuf.readInt();
        if (result==0){
            logger.info(LogUtil.buildLog("告警模式设置失败：",null));
        }else{
            logger.info(LogUtil.buildLog("告警模式设置成功：",result));
        }
        return baseInfo;
    }

    /**
     * 接收到解码后的数据 并进行处理
     *
     * @param obj
     */
    @Override
    public void handle(Object obj) {

    }
}