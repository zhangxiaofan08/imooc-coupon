package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 结算微服务熔断策略实现
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 11:09
 */
@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {
    /**
     * 优惠券规则计算
     *
     * @param settlementInfo
     * @return
     * @throws CouponException
     */
    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlementInfo) throws CouponException {
        log.error("[eureka-client-coupon-settlement] computeRule request error");

        settlementInfo.setEmploy(false);
        settlementInfo.setCost(-1.0);

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-settlement] computeRule request error",
                settlementInfo
        );
    }
}
