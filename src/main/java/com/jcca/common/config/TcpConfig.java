package com.jcca.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description: application.yml配置文件中的配置项
 * @date: 2023/11/24 9:04
 * @author: sophia
 */
@Data
@Component
public class TcpConfig {

    // TCP相关配置
    @Value("${tcp.host}")
    private String tcpIp;
    @Value("${tcp.port}")
    private int tcpPort;
    @Value("${tcp.port2}")
    private int tcpPort2;
    @Value("${tcp.open}")
    private int tcpOpen;
    @Value("${tcp.username}")
    private String username;
    @Value("${tcp.password}")
    private String password;
    @Value("${tcp.receive-timeout}")
    private Long tcpReceiveTimeout;


    //itsm 对接动环机房关联关系
    @Value("${tcp.roomId1}")
    private String roomId1;
    @Value("${tcp.station1}")
    private String station1;
    @Value("${tcp.roomId2}")
    private String roomId2;
    @Value("${tcp.station2}")
    private String station2;

}
