package com.jcca.supervision.tcp;

import com.jcca.supervision.constant.DataConst;
import com.jcca.supervision.data.frame.BaseDataFrame;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

/**
 * @description: netty服务类，处理对外消息发送
 * @classname: NettyService
 * @date: 2022/4/19 17:14
 * @author: hanwone
 * @since: 2.0.0.1
 */
@Service
public class NettyService {

    /**
     * 发送通号数据请求
     *
     * @param dataFrame 要发送的数据
     */
    public void sendTcpMsg(BaseDataFrame dataFrame) {
        Channel channel = (Channel) DataConst.TEMP_MAP.get(DataConst.NETTY_TCP_CHANNEL);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(dataFrame);
        }
    }
}
