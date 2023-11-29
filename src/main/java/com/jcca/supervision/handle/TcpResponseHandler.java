package com.jcca.supervision.handle;

import com.jcca.common.LogUtil;
import com.jcca.common.config.TcpConfig;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @description 消息处理器
 * @author sophia
 * @date 2023/11/25 16:02
 */
@Component
public class TcpResponseHandler implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());

    Map<Integer, ResponseHandleAdapter> handleAdapterMap;

    /**
     * 去解码
     *
     * @param code       程序处理代码
     * @param contentBuf 消息数据
     * @return obj
     */
    public Object decode(Integer code, ByteBuf contentBuf) {
        ResponseHandleAdapter handleAdapter = handleAdapterMap.get(code);
        if (Objects.isNull(handleAdapter)) {
            logger.error(LogUtil.buildLog("没有找到对应的解码适配器", code + ""));
            return null;
        }
        return handleAdapter.decode(contentBuf);
    }

    /**
     * 业务处理
     *
     * @param code 程序处理代码
     * @param obj  业务数据
     */
    public void handle(Integer code, Object obj) {
        ResponseHandleAdapter handleAdapter = handleAdapterMap.get(code);
        if (Objects.isNull(handleAdapter)) {
            logger.error(LogUtil.buildLog("没有找到对应的业务处理适配器", code + ""));
            return;
        }
        handleAdapter.handle(obj);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TcpConfig config = applicationContext.getBean(TcpConfig.class);
        if (config.getTcpOpen() != 1) return;

        if (handleAdapterMap == null) {
            Map<String, ResponseHandleAdapter> beanMap =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ResponseHandleAdapter.class, true, false);
            handleAdapterMap = new HashMap<>(beanMap.size());
            for (Map.Entry<String, ResponseHandleAdapter> entry : beanMap.entrySet()) {
                if (handleAdapterMap.containsKey(entry.getValue().getCode())) {
                    throw new RuntimeException(
                            "初始化处理适配器失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
                } else {
                    handleAdapterMap.put(entry.getValue().getCode(), entry.getValue());
                }
            }
            logger.info(LogUtil.buildLog("初始化处理适配器完成", handleAdapterMap.size() + ""));
        }
    }
}
