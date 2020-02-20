package com.imooc.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @AUTHOR zhangxf
 * @CREATE 2020-02-10 23:48
 */
@Slf4j
@Component
public class AccessLogFilter extends AbstractPostZuulFilter {
    @Override
    Object cRun() {
        HttpServletRequest request = context.getRequest();
        //从PreRequestFilter中获取设置的起始时间
        Long startTime = (Long) context.get("startTime");
        String uri = request.getRequestURI();
        long duration = System.currentTimeMillis() - startTime;
        //从网关通过的请求都会打印日志记录： uri + duration
        log.info("uri : {}, duration : {}", uri, duration);
        return success();
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER - 1;
    }
}
