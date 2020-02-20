package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

/**
 * 分发系统的启动入口
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 11:05
 */
@EnableFeignClients //通过feign调用其他微服务
@EnableJpaAuditing  //jpa审计功能
@EnableCircuitBreaker   //熔断降级功能
@EnableEurekaClient
@SpringBootApplication
public class DistributionApplication {
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class, args);
    }
}
