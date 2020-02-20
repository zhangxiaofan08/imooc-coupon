package com.imooc.coupon.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * 校验请求中token
 * @AUTHOR zhangxf
 * @CREATE 2020-02-10 23:26
 */
@Slf4j
// @Component
public class TokenFilter extends AbstractPreZuulFilter {
    @Override
    Object cRun() {
        HttpServletRequest request = context.getRequest();
        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        Object token = request.getParameter("token");
        if (null == token) {
            log.error("error: token is empty");
            return fail(401, "error: token is empty");
        }
        //成功的话给next赋值true，过滤器继续往下执行
        return success();
    }

    @Override
    public int filterOrder() {
        return 1;
    }
}
