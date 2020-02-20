package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模版基础（view，delete。。。）服务定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 17:39
 */
public interface ITemplateBaseService {
    /**
     * 根据优惠券模版id获取优惠券模版信息
     * @param id
     * @return
     * @throws CouponException
     */
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

    /**
     * 查找所有可用的优惠券模版
     * @return
     */
    List<CouponTemplateSDK> findAllUsableTemplate();

    /**
     * 获取模版 ids 到 CouponTemplateSDK 的映射
     * @param ids
     * @return Map<key : 模版id, value : CouponTemplateSDK>
     */
    Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
