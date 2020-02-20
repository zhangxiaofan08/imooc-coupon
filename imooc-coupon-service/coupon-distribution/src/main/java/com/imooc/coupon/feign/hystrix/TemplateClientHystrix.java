package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 优惠券模版 Feign 接口的熔断降级策略
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 11:03
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {

    /**
     * 查找所有可用的优惠券模版
     *
     * @return
     */
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate request error");
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] findAllUsableTemplate request error",
                Collections.emptyList());
    }

    /**
     * 获取模版ids到CouponTemplateSDK的映射
     *
     * @param ids 优惠券模版ids
     * @return
     */
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2SDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2SDK request error");
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] findIds2SDK request error",
                new HashMap<>()
        );
    }
}
