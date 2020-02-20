package com.imooc.coupon.service.impl;

import com.google.common.base.Stopwatch;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 异步服务接口实现
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 20:21
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    private final CouponTemplateDao templateDao;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public AsyncServiceImpl(CouponTemplateDao templateDao, StringRedisTemplate redisTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
    }

    @Async("getAsyncExecutor")
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();

        Set<String> couponCodes = buildCouponCode(template);

        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, template.getId().toString());

        //放入redis中并打印日志
        log.info("Push CouponCode To Redis: {}", redisTemplate.opsForList().rightPushAll(redisKey, couponCodes));

        template.setAvailable(true);

        templateDao.save(template);

        watch.stop();
        log.info("Construct CouponCode By Template Cost: {}ms", watch.elapsed(TimeUnit.MILLISECONDS));

        // TODO: 2020-02-12 发送短信或邮件通知优惠券模版已经可用
        log.info("CouponTemplate({}) Is Available!", template.getId());

    }

    /**
     * 构造优惠券码
     * 优惠券码对应于每一张优惠券，18位
     * 前四位：产品线 + 类型
     * 中间六位：日期随机（190101）
     * 后八位：0～9随机数构成
     *
     * @param template
     * @return Set<String> 于template.count 相同个数的优惠券码
     */
    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();

        Set<String> result = new HashSet<>(template.getCount());

        String prefix_4 = template.getProductLine().getCode().toString() + template.getCategory().getCode();
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreateTime());

        for (int i = 0; i != template.getCount(); i++) {
            result.add(prefix_4 + buildCouponCodeSuffix_14(date));
        }

        //随机数可能重复，去重
        while (result.size() < template.getCount()) {
            result.add(prefix_4 + buildCouponCodeSuffix_14(date));
        }

        assert result.size() == template.getCount();

        watch.stop();

        log.info("build coupon code cost: {}ms", watch.elapsed(TimeUnit.MILLISECONDS));

        return result;

    }

    /**
     * 构造优惠券码的后 14 位
     *
     * @param date 创建优惠券的日期
     * @return 14 位优惠券码
     */
    private String buildCouponCodeSuffix_14(String date) {
        char[] bases = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};
        //中间六位
        List<Character> chars = date.chars().mapToObj(e -> (char) e).collect(Collectors.toList());

        //洗牌算法
        Collections.shuffle(chars);

        String mid_6 = chars.stream().map(Object::toString).collect(Collectors.joining());

        String suffix_8 = RandomStringUtils.random(1, bases) + RandomStringUtils.randomNumeric(7);

        return mid_6 + suffix_8;
    }
}
