package com.jcca.supervision.handle;

import io.netty.buffer.ByteBuf;

/**
 * @author sophia
 * @description 程序处理
 * @date 2023/11/27 16:10
 */
public interface ResponseHandleAdapter {

    /**
     * 根据程序处理代码和返回信息对应
     *
     * @return 程序处理代码
     */
    Integer getCode();

    /**
     * 对不同响应消息进行解码
     *
     * @param contentBuf 要解码的数据
     * @return Object
     */
    Object decode(ByteBuf contentBuf);


    /**
     * 业务处理
     */
    void handle(Object obj);
}
