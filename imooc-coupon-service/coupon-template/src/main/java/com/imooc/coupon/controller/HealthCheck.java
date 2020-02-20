package com.imooc.coupon.controller;

import com.imooc.coupon.exception.CouponException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康检测接口
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 23:29
 */
@Slf4j
@RestController
public class HealthCheck {
    /**
     * 服务发现客户端
     */
    private final DiscoveryClient client;

    /**
     * 服务注册接口，提供了获取服务 id 的方法
     */
    private final Registration registration;

    @Autowired
    public HealthCheck(DiscoveryClient client, Registration registration) {
        this.client = client;
        this.registration = registration;
    }

    /**
     * 健康检测接口
     * localhost:7001/coupon-template/health
     *
     * @return
     */
    @GetMapping("/health")
    public String health() {
        log.debug("view health api");
        return "CouponTemplate Is OK!";
    }

    /**
     * 异常测试接口
     * localhost:7001/coupon-template/exception
     *
     * @return
     */
    @GetMapping("/exception")
    public String exception() throws CouponException {
        log.debug("view exception api");
        throw new CouponException("CouponTemplate Has Some Problem");
    }

    /**
     * 获取 Eureka Server 上的微服务元信息
     *
     * @return
     */
    @GetMapping("/info")
    public List<Map<String, Object>> info() {

        // 大约需要等待两分钟才能获取到注册信息
        List<ServiceInstance> instanceList = client.getInstances(registration.getServiceId());

        List<Map<String, Object>> result = new ArrayList<>(instanceList.size());

        instanceList.forEach(instance -> {
            Map<String, Object> info = new HashMap<>();

            info.put("serviceId", instance.getServiceId());
            info.put("instanceId", instance.getInstanceId());
            info.put("port", instance.getPort());

            result.add(info);
        });

        return result;
    }
}
