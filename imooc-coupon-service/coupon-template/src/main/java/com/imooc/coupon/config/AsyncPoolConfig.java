package com.imooc.coupon.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义异步任务线程池
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 16:31
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncPoolConfig implements AsyncConfigurer {
    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(32);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Imooc_Async_");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            throwable.printStackTrace();
            log.error("AsyncError: {}, Method: {}, Param: {}", throwable.getMessage(), 
                    method.getName(), JSON.toJSONString(objects));
            // TODO: 2020-02-12 发送邮件或者短线做进一步处理
        };
    }
}
