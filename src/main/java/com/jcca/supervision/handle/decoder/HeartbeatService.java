package com.jcca.supervision.handle.decoder;

import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.handle.ResponseHandleAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author sophia
 * @description 心跳数据
 * @date 2023/11/27 9:34
 */
@Service
public class HeartbeatService implements ResponseHandleAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    @Override
    public Integer getCode() {
        return DataConst.HEART_BEAT;
    }

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    @Override
    public Object decode(ByteBuf contentBuf) {
        // 心跳消息不处理
        String heartInfo = ByteBufUtil.hexDump(contentBuf);
       // logger.info(LogUtil.buildLog("收到心跳回复", heartInfo));
        contentBuf.clear();
        return null;
    }

    /**
     * 业务处理
     *
     * @param obj 业务数据
     */
    @Override
    public void handle(Object obj) {
        // 心跳消息不处理
    }
}
