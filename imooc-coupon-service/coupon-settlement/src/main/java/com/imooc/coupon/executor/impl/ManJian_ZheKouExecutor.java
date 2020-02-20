package com.imooc.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 满减+折扣优惠券结算规则执行期
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 19:50
 */
@Slf4j
@Component
public class ManJian_ZheKouExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * 规则类型标记
     *
     * @return
     */
    @Override
    public RuleFlag ruleFlag() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    /**
     * 校验商品类型与优惠券是否匹配
     * 需要注意：
     * 1、这里实现的是满减+折扣优惠券的校验
     * 2、如果想要使用多类优惠券，则必须要所有的商品类型都包含在内
     *
     * @param settlementInfo
     * @return
     */
    @Override
    @SuppressWarnings("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlementInfo) {
        log.debug("Check ManJian And ZheKou Is Match Or Not!");
        List<Integer> goodsType = settlementInfo.getGoodsInfos()
                .stream()
                .map(GoodsInfo::getType)
                .collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();
        settlementInfo.getCouponAndTemplateInfos().forEach(ct -> {
            templateGoodsType.addAll(
                    JSON.parseObject(
                            ct.getTemplate()
                                    .getRule()
                                    .getUsage()
                                    .getGoodsType(),
                            List.class
                    ));
        });

        // 如果想要使用多类优惠券，则必须要所有的商品类型都包含在内，即差集为空
        return CollectionUtils.isEmpty(CollectionUtils.subtract(goodsType, templateGoodsType));
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
        //商品类型的校验
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlementInfo, goodsSum);
        if (null != probability) {
            log.debug("ManJian And ZheKou Template Is Not Match To GoodsType!");
            return probability;
        }

        SettlementInfo.CouponAndtemplateInfo manJian = null;
        SettlementInfo.CouponAndtemplateInfo zheKou = null;
        for (SettlementInfo.CouponAndtemplateInfo ct : settlementInfo.getCouponAndTemplateInfos()) {
            if (CouponCategory.of(ct.getTemplate().getCategory()) == CouponCategory.MANJIAN) {
                manJian = ct;
            } else {
                zheKou = ct;
            }
        }
        assert null != manJian;
        assert null != zheKou;
        //当前的优惠券和满减券如果不能一起使用，清空优惠券，返回商品原价
        if (!isTemplateCanShared(manJian, zheKou)) {
            log.debug("Current Manjian And Zhekou Cannot Shared!");
            settlementInfo.setCost(goodsSum);
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }

        List<SettlementInfo.CouponAndtemplateInfo> ctInfos = new ArrayList<>();
        double manJianBase = (double) manJian.getTemplate().getRule().getDiscount().getBase();
        double manJianQuota = (double) manJian.getTemplate().getRule().getDiscount().getQuota();
        //最终价格 满减 + 折扣
        double targetSum = goodsSum;
        if (targetSum >= manJianBase) {
            targetSum -= manJianQuota;
            ctInfos.add(manJian);
        }
        double zheKouQuota = (double) zheKou.getTemplate().getRule().getDiscount().getQuota();
        targetSum *= zheKouQuota * 1.0 / 100;
        ctInfos.add(zheKou);

        settlementInfo.setCost(
                retain2Decimals(
                        targetSum > minCost() ? targetSum : minCost()
                )
        );
        settlementInfo.setCouponAndTemplateInfos(ctInfos);
        log.debug("Use ManJian And ZheKou Coupon Make Goods Cost From {} To {}", goodsSum, settlementInfo.getCost());

        return settlementInfo;
    }

    /**
     * 当前的两张优惠券是否可以共用
     *
     * @param manJian
     * @param zheKou
     * @return
     */
    @SuppressWarnings("all")
    private boolean isTemplateCanShared(SettlementInfo.CouponAndtemplateInfo manJian,
                                        SettlementInfo.CouponAndtemplateInfo zheKou) {
        String manJianKey = manJian.getTemplate().getKey() + String.format("%04d", manJian.getTemplate().getId());
        String zheKouKey = zheKou.getTemplate().getKey() + String.format("%04d", zheKou.getTemplate().getId());

        List<String> allSharedKeysForManJian = new ArrayList<>();
        allSharedKeysForManJian.add(manJianKey);
        allSharedKeysForManJian.addAll(
                JSON.parseObject(
                        manJian.getTemplate()
                                .getRule()
                                .getWeight(),
                        List.class
                )
        );

        List<String> allSharedKeysForZheKou = new ArrayList<>();
        allSharedKeysForZheKou.add(zheKouKey);
        allSharedKeysForZheKou.addAll(
                JSON.parseObject(
                        zheKou.getTemplate()
                                .getRule()
                                .getWeight(),
                        List.class
                )
        );

        return CollectionUtils.isSubCollection(Arrays.asList(manJianKey, zheKouKey), allSharedKeysForManJian)
                || CollectionUtils.isSubCollection(Arrays.asList(manJianKey, zheKouKey), allSharedKeysForZheKou);
    }

}
