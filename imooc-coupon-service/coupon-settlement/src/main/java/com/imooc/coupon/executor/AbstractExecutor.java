package com.imooc.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则执行器抽象类，定义通用方法
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 17:48
 */
public abstract class AbstractExecutor {
    /**
     * 校验商品类型与优惠券是否匹配
     * 需要注意：
     * 1、这里实现的是单品类优惠券的校验，多品类优惠券重载此方法
     * 2、商品只需要一个优惠券要求的商品类型去匹配就可以
     *
     * @param settlementInfo
     * @return
     */
    @SuppressWarnings("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlementInfo) {
        List<Integer> goodsType = settlementInfo.getGoodsInfos()
                .stream()
                .map(GoodsInfo::getType)
                .collect(Collectors.toList());
        List<Integer> templateGoodsType = JSON.parseObject(
                settlementInfo.getCouponAndTemplateInfos()
                        .get(0)
                        .getTemplate()
                        .getRule()
                        .getUsage()
                        .getGoodsType(),
                List.class
        );
        // 存在交集即可
        return CollectionUtils.isNotEmpty(
                CollectionUtils.intersection(goodsType, templateGoodsType)
        );
    }

    /**
     * 处理商品类型与优惠券限制不匹配的情况
     *
     * @param settlementInfo
     * @param goodsSum       商品总价
     * @return
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlementInfo, double goodsSum) {
        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfy(settlementInfo);
        //当商品类型不满足时，直接返回总价，并清空优惠券
        if (!isGoodsTypeSatisfy) {
            settlementInfo.setCost(goodsSum);
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }
        return null;
    }

    /**
     * 商品总价
     *
     * @param goodsInfos
     * @return
     */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos) {
        return goodsInfos.stream().mapToDouble(
                goods -> goods.getPrice() * goods.getCount()
        ).sum();
    }

    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    protected double retain2Decimals(double value) {
        return new BigDecimal(value)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * 最小支付费用，防止优惠后出现负数
     * @return
     */
    protected double minCost() {
        return 0.1;
    }
}
