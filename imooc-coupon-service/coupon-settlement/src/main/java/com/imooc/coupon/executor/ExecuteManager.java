package com.imooc.coupon.executor;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券结算规则执行管理器
 * 根据用户的请求（settlementinfo）找到对应的Executor，去做结算
 * BeanPostProcessor: Bean后置处理器，所有的Bean创建完成后开始执行
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 21:11
 */
@Slf4j
@Component
public class ExecuteManager implements BeanPostProcessor {
    /**
     * 规则执行器映射
     */
    private static Map<RuleFlag, RuleExecutor> executorMap = new HashMap<>(RuleFlag.values().length);

    /**
     * 优惠券结算规则计算入口
     * 注意：一定要保证传递过来的优惠券个数 >= 1
     *
     * @param settlementInfo
     * @return
     * @throws CouponException
     */
    public SettlementInfo computeRule(SettlementInfo settlementInfo) throws CouponException {
        SettlementInfo result = null;
        //单类优惠券
        if (settlementInfo.getCouponAndTemplateInfos().size() == 1) {
            //获取优惠券的类别
            CouponCategory category = CouponCategory.of(
                    settlementInfo.getCouponAndTemplateInfos()
                            .get(0)
                            .getTemplate()
                            .getCategory()
            );

            switch (category) {
                case MANJIAN:
                    result = executorMap.get(RuleFlag.MANJIAN).computeRule(settlementInfo);
                    break;
                case LIJIAN:
                    result = executorMap.get(RuleFlag.LIJIAN).computeRule(settlementInfo);
                    break;
                case ZHEKOU:
                    result = executorMap.get(RuleFlag.ZHEKOU).computeRule(settlementInfo);
                    break;
            }
        } else {
            //多类优惠券
            List<CouponCategory> categories = new ArrayList<>(
                    settlementInfo.getCouponAndTemplateInfos().size()
            );
            settlementInfo.getCouponAndTemplateInfos().forEach(ct -> categories.add(
                    CouponCategory.of(ct.getTemplate().getCategory())
            ));
            if (categories.size() != 2) {
                throw new CouponException("Not Support For More Template Category.");
            } else {
                if (categories.contains(CouponCategory.MANJIAN) && categories.contains(CouponCategory.ZHEKOU)) {
                    result = executorMap.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(settlementInfo);
                } else {
                    throw new CouponException("Not Support For More Template Category.");
                }
            }
        }
        return result;
    }

    /**
     * 在bean创建后，初始化之前去执行
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof RuleExecutor)) {
            return bean;
        }
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleFlag();
        if (executorMap.containsKey(ruleFlag)) {
            throw new IllegalStateException("There is already an executor for rule flag: " + ruleFlag);
        }
        log.info("Load executor {} for rule flag {}.", executor.getClass(), ruleFlag);
        executorMap.put(ruleFlag, executor);
        return null;
    }

    /**
     * 在bean创建后，初始化之后去执行
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
