package com.jcca.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName AsyncConfig
 * @Description 异步配置
 * @Date 2020/4/17 17:34
 * @Author hanwone
 */
@Configuration
public class AsyncConfig {

    /**
     * WARNNING 新的线程池需要另起名字创建，不建议使用相同名字的线程池处理不同的业务
     */

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return getExecutor("taskExecutor-");
    }

    private Executor getExecutor(String namePrefix) {
        int core = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(namePrefix);
        executor.setCorePoolSize(core * 2);
        executor.setQueueCapacity(20000);
        executor.setMaxPoolSize(core * 4 + 1);
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    // 固定频率线程
    public static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(8);


}
