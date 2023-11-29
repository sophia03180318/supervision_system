package com.jcca.supervision.data.frame;

import lombok.Data;
import java.io.Serializable;

/**
 * @author sophia
 * @description 报文数据
 * @date 2023/11/28 9:12
 */
@Data
public class BaseDataFrame implements Serializable {

    /**
     * 报文长度 4字节
     * */
    private int len;

    /**
     * 报文序号 4字节
     * */
    private int num;

    /**
     * 报文类型 4字节
     * */
    private int type;


}
