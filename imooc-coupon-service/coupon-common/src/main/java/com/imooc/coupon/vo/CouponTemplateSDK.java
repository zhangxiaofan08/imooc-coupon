package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微服务之间的优惠券模版信息定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 16:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateSDK {
    /**
     * 优惠券模版主键
     */
    private Integer id;
    /**
     * 优惠券模版名称
     */
    private String name;
    /**
     * 优惠券logo
     */
    private String logo;
    /**
     * 优惠券描述
     */
    private String desc;
    /**
     * 优惠券分类
     */
    private String category;
    /**
     * 产品线
     */
    private Integer productLine;
    /**
     * 优惠券模版的编码
     */
    private String key;
    /**
     * 目标用户
     */
    private Integer target;
    /**
     * 优惠券规则
     */
    private TemplateRule rule;
}
