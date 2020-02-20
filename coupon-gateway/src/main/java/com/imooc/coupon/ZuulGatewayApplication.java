package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @AUTHOR zhangxf
 * @CREATE 2020-02-10 22:43
 *
 * SpringCloudApplication 注解组合了SpringBoot应用 + 服务发现 + 熔断
 */
@EnableZuulProxy
@SpringCloudApplication
public class ZuulGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class, args);
    }
}
