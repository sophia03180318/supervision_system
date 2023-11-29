package com.jcca.common.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HanHW
 * @description 公用常量
 * @className BrokerConst
 * @date 2023/6/6 15:41
 * @since 2.0.4.0
 */
public interface BrokerConst {
    // 存放临时数据
    Map<String, Object> TEMP_MAP = new ConcurrentHashMap<>();

    Integer ABFLAG_A = 0;

    Integer ABFLAG_B = 1;
}
