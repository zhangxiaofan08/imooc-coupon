package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.DistributeTarget;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.constant.ProductLine;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.TemplateRequest;
import com.imooc.coupon.vo.TemplateRule;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 构造优惠券模版服务测试
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-13 00:46
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    /**
     * 创建优惠券模版
     *
     * @param
     * @return
     */
    @Test
    public void  testBuildTemplate() throws CouponException, InterruptedException {
        System.out.println(JSON.toJSONString(buildTemplateService.buildTemplate(fakeTemplateRequest())));
        // 为了保证异步方法能够执行完
        TimeUnit.SECONDS.sleep(5);
    }

    private TemplateRequest fakeTemplateRequest() {
        TemplateRequest request = new TemplateRequest();

        request.setName("优惠券模版-" + new Date().getTime());
        request.setLogo("http://www.imooc.com");
        request.setDesc("这是一张优惠券");
        request.setCategory(CouponCategory.MANJIAN.getCode());
        request.setProductLine(ProductLine.DAMAO.getCode());
        request.setCount(10000);
        request.setUserId(10001L);
        request.setTarget(DistributeTarget.SINGLE.getCode());

        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(),
                1,
                DateUtils.addDays(new Date(), 60).getTime()
        ));
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage(
                "安徽省", "同城市",
                JSON.toJSONString(Arrays.asList("文娱", "家居"))
        ));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));
        request.setRule(rule);

        return request;
    }
}
