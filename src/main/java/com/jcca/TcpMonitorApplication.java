package com.jcca;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableSwagger2
@MapperScan(basePackages = "com.jcca.**.mapper")
public class TcpMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcpMonitorApplication.class, args);
    }

}
