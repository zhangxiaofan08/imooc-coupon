package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.TemplateRequest;

/**
 * 构建优惠券模版接口定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 16:25
 */
public interface IBuildTemplateService {
    /**
     * 创建优惠券模版
     * @param request {@link TemplateRequest} 模版信息请求对象
     * @return {@link CouponTemplate} 优惠券模版实体
     * @throws CouponException
     */
    CouponTemplate buildTemplate(TemplateRequest request) throws CouponException;
}
