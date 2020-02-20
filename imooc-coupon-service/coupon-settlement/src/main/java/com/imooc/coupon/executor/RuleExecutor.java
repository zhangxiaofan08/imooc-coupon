package com.imooc.coupon.executor;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.vo.SettlementInfo;

/**
 * 优惠券模版规则处理器接口定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 17:44
 */
public interface RuleExecutor {
    /**
     * 规则类型标记
     * @return
     */
    RuleFlag ruleFlag();

    /**
     * 优惠券规则计算
     * @param settlementInfo    包含了选择的优惠券
     * @return  修正过的结算信息
     */
    SettlementInfo computeRule(SettlementInfo settlementInfo);
}
