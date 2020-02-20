package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 满减优惠券结算规则执行器
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 18:39
 */
@Slf4j
@Component
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * 规则类型标记
     *
     * @return
     */
    @Override
    public RuleFlag ruleFlag() {
        return RuleFlag.MANJIAN;
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
            log.debug("Manjian Template Is Not Match To GoodsType!");
            return probability;
        }
        //判断满减是否符合折扣标准
        CouponTemplateSDK templateSDK = settlementInfo.getCouponAndTemplateInfos().get(0).getTemplate();
        double base = (double) templateSDK.getRule().getDiscount().getBase();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();
        //如果不符合标准，则直接返回商品总价
        if (goodsSum < base) {
            log.debug("Current Goods Cost Sum < ManJian Coupon Base!");
            settlementInfo.setCost(goodsSum);
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }
        //计算使用优惠券之后的价格 -- 结算
        settlementInfo.setCost(retain2Decimals((goodsSum - quota > minCost()) ? (goodsSum - quota) : minCost()));

        log.debug("Use ManJian Coupon Make Goods Cost From {} To {}", goodsSum, settlementInfo.getCost());

        return settlementInfo;
    }
}
