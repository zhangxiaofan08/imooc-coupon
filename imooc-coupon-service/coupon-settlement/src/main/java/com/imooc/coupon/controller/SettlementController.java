package com.imooc.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.executor.ExecuteManager;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结算服务的Controller
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 21:41
 */
@Slf4j
@RestController
public class SettlementController {
    /**
     * 结算规则执行管理器
     */
    private final ExecuteManager executeManager;


    @Autowired
    public SettlementController(ExecuteManager executeManager) {
        this.executeManager = executeManager;
    }

    /**
     * 优惠券结算
     * localhost:9000/imooc/coupon-settlement/settlement/compute
     * @param settlementInfo
     * @return
     * @throws CouponException
     */
    @RequestMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlementInfo) throws CouponException {
        log.info("settlement: {}", JSON.toJSONString(settlementInfo));
        return executeManager.computeRule(settlementInfo);
    }
}
