package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 优惠券结算微服务的启动入口
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 17:25
 */
@EnableEurekaClient
@SpringBootApplication
public class SettlementApplication {
    public static void main(String[] args) {
        SpringApplication.run(SettlementApplication.class, args);
    }
}
