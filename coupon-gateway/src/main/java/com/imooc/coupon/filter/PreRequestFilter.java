package com.imooc.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @AUTHOR zhangxf
 * @CREATE 2020-02-10 23:46
 */
@Slf4j
@Component
public class PreRequestFilter extends AbstractPreZuulFilter {
    @Override
    Object cRun() {
        context.set("startTime", System.currentTimeMillis());
        return success();
    }

    @Override
    public int filterOrder() {
        return 0;
    }
}
