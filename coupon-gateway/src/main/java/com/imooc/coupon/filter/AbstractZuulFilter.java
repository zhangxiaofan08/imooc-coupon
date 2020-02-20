package com.imooc.coupon.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * @AUTHOR zhangxf
 * @CREATE 2020-02-10 23:07
 */
public abstract class AbstractZuulFilter extends ZuulFilter {

    /*
    用于在过滤器之间传递消息，数据保存在每个请求的ThreadLocal中
    扩展了Map
     */
    RequestContext context;

    private final static String NEXT = "next";

    /**
     * 返回true才能继续执行run方法
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return (boolean)ctx.getOrDefault(NEXT, true);
    }

    @Override
    public Object run() throws ZuulException {
        context = RequestContext.getCurrentContext();
        return cRun();
    }

    abstract Object cRun();

    Object fail(int code, String msg) {
        context.set(NEXT, false);
        context.setSendZuulResponse(false);
        context.getResponse().setContentType("text/html;charset=UTF-8");
        context.setResponseStatusCode(code);
        context.setResponseBody(String.format("{\"result\" : \"%s!\"}", msg));
        return null;
    }

    Object success(){
        context.set(NEXT, true);
        return null;
    }
}
