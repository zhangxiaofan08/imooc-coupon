package com.imooc.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @AUTHOR zhangxf
 * @CREATE 2020-02-10 23:22
 */
public abstract class AbstractPostZuulFilter extends AbstractZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
