package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 折扣优惠券结算规则执行期
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 18:57
 */
@Slf4j
@Component
public class ZheKouExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * 规则类型标记
     *
     * @return
     */
    @Override
    public RuleFlag ruleFlag() {
        return RuleFlag.ZHEKOU;
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
            log.debug("ZheKou Template Is Not Match GoodsType!");
            return probability;
        }
        //折扣优惠券可以直接使用，没有门槛
        CouponTemplateSDK templateSDK = settlementInfo.getCouponAndTemplateInfos()
                .get(0)
                .getTemplate();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();

        //计算使用优惠券之后的价格
        double cost = retain2Decimals(goodsSum * (quota * 1.0 / 100));
        settlementInfo.setCost(
                cost > minCost() ? cost : minCost()
        );
        log.debug("Use ZheKou Coupon Make Goods Cost From {} to {}", goodsSum, settlementInfo.getCost());
        return settlementInfo;
    }
}
