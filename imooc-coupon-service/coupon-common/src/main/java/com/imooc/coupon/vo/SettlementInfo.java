package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结算信息
 * 包含：
 * 1、userId
 * 2、商品信息（列表）
 * 3、优惠券列表
 * 4、结算结果金额
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 16:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品信息
     */
    private List<GoodsInfo> goodsInfos;
    /**
     * 优惠券列表
     */
    private List<CouponAndtemplateInfo> couponAndTemplateInfos;

    /**
     * 是否是结算生效--即核销
     */
    private Boolean employ;

    /**
     * 结果结算金额
     */
    private Double cost;

    /**
     * 优惠券和模版信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponAndtemplateInfo {
        /**
         * coupon的主键
         */
        private Integer id;

        /**
         * 优惠券对应的模版对象
         */
        private CouponTemplateSDK template;
    }
}
