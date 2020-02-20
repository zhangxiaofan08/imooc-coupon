package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 立减优惠券结算规则执行器
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 19:16
 */
@Slf4j
@Component
public class LiJianExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * 规则类型标记
     *
     * @return
     */
    @Override
    public RuleFlag ruleFlag() {
        return RuleFlag.LIJIAN;
    }

    /**
     * 优惠券规则计算
     *
     * @param settlementInfo 包含了选择的优惠券
     * @return 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlementInfo) {
        double goodsSum = retain2Decimals(goodsCostSum(settlementInfo.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlementInfo, goodsSum);
        if (null != probability) {
            log.debug("LiJian Template Is Not Match To GoodsType!");
            return probability;
        }

        //立减优惠券直接使用，没有门槛
        CouponTemplateSDK templateSDK = settlementInfo.getCouponAndTemplateInfos().get(0).getTemplate();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();
        //计算使用优惠券之后的价格 -- 结算
        double cost = retain2Decimals(goodsSum - quota);
        settlementInfo.setCost(cost > minCost() ? cost : minCost());

        log.debug("Use LiJian Coupon Make Goods Cost From {} To {}", goodsSum, settlementInfo.getCost());

        return settlementInfo;
    }
}
