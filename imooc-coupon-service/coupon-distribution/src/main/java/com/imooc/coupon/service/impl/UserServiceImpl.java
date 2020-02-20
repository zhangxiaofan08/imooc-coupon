package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.service.IRedisService;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务相关的接口实现
 * 所有的操作过程、状态都保存在redis中
 * 通过Kafka把消息传递到Mysql中
 * 为什么使用kafka，而不是直接使用Springboot中的异步处理？
 * 安全性，一致性
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 11:48
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final CouponDao couponDao;

    private final IRedisService redisService;

    /**
     * 模版微服务客户端
     */
    private final TemplateClient templateClient;

    /**
     * 结算微服务客户端
     */
    private final SettlementClient settlementClient;

    /**
     * Kafka客户端
     */

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(CouponDao couponDao, IRedisService redisService,
                           TemplateClient templateClient,
                           SettlementClient settlementClient,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 根据用户ID和状态查询优惠券记录
     *
     * @param userId
     * @param status
     * @return
     * @throws CouponException
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {

        List<Coupon> currentCachedList = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;

        if (CollectionUtils.isNotEmpty(currentCachedList)) {
            log.debug("coupon cache is not empty: {}, {}", userId, status);
            preTarget = currentCachedList;
        } else {
            log.debug("coupon cache is empty, get coupon from db: {}, {}", userId, status);
            List<Coupon> dbCouponList = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));

            // 如果数据库中没有记录，直接返回就可以，Cache中已经加入了一张无效的优惠券
            if (CollectionUtils.isEmpty(dbCouponList)) {
                log.debug("current user donot have coupon: {}, {}", userId, status);
                return dbCouponList;
            }

            //填充dbCouponList的templateSDK字段
            Map<Integer, CouponTemplateSDK> id2SDK = templateClient.findIds2SDK(
                    dbCouponList.stream().map(Coupon::getTemplateId).collect(Collectors.toList())
            ).getData();

            dbCouponList.forEach(coupon -> coupon.setTemplateSDK(id2SDK.get(coupon.getTemplateId())));
            //数据库中存在记录
            preTarget = dbCouponList;
            //将记录写入Cache
            redisService.addCouponToCache(userId, preTarget, status);

        }
        //将无效优惠券剔除
        preTarget = preTarget.stream().filter(coupon -> coupon.getId() != -1).collect(Collectors.toList());
        //如果当前获取的是可用的优惠券，还需要做对已过期的优惠券的延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            //如果已过期状态不为空，需要做延迟处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("Add Expired Coupons To Cache From FindCouponsByStatus: {}, {}", userId, status);
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());
                //发送到Kafka中做异步处理
                kafkaTemplate.send(Constant.TOPIC,
                        JSON.toJSONString(
                                new CouponKafkaMessage(
                                        CouponStatus.EXPIRED.getCode(),
                                        classify.getExpired().stream()
                                                .map(Coupon::getId)
                                                .collect(Collectors.toList())
                                )
                        )
                );
            }
            return classify.getUsable();
        }
        return preTarget;
    }

    /**
     * 根据用户 id 查找当前可以领取的优惠券模版
     *
     * @param userId
     * @return
     * @throws CouponException
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = new Date().getTime();
        List<CouponTemplateSDK> sdkList = templateClient.findAllUsableTemplate().getData();

        log.debug("Find All Template(From TemplateClient) Count: {}", sdkList.size());
        //过滤过期的优惠券模版
        sdkList = sdkList.stream().filter(sdk ->
                sdk.getRule().getExpiration().getDeadline() > curTime
        ).collect(Collectors.toList());

        log.info("Find Usable Template Count: {}", sdkList);

        /*
        key : TemplateId
        value :
            left : Template limitation
            right: coupon template
         */
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(sdkList.size());
        sdkList.forEach(sdk -> limit2Template.put(sdk.getId(), Pair.of(sdk.getRule().getLimitation(), sdk)));

        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCouponList = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());

        log.debug("Current User Has Usable Coupons: {}, {}", userId, userUsableCouponList);

        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCouponList.stream().
                collect(Collectors.groupingBy(Coupon::getTemplateId));
        //根据template 的rule 判断是否可以领取优惠券模版
        limit2Template.forEach((k, v) -> {

            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();

            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    /**
     * 用户领取优惠券
     * 1、从TemplateClient中拿到相对应的优惠券，并检查是否过期
     * 2、根据limitation判断用户是否可以领取
     * 3、save to db
     * 4、填充CouponTemplateSDK
     * 5、save to cache
     *
     * @param request
     * @return
     * @throws CouponException
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        //根据id获取优惠券模版
        Map<Integer, CouponTemplateSDK> id2Template = templateClient.findIds2SDK(
                Collections.singletonList(request.getTemplateSDK().getId())
        ).getData();

        //优惠券模版是需要存在的
        if (id2Template.size() <= 0) {
            log.error("Cannot Acquire Template From TemplateClient: {}", request.getTemplateSDK().getId());
            throw new CouponException("Cannot Acquire Template From TemplateClient");
        }
        //用户是否可以领取这张优惠券
        List<Coupon> userUsableCoupons = findCouponsByStatus(request.getUserId(), CouponStatus.USABLE.getCode());
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())
                && templateId2Coupons.get(request.getTemplateSDK().getId()).size()
                >= request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Execed Template Assign Limitation: {}", request.getTemplateSDK().getId());
            throw new CouponException("Execed Template Assign Limitation!");
        }

        //尝试获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Cannot Acquire Coupon Code: {}", request.getTemplateSDK().getId());
            throw new CouponException("Cannot Acquire Coupon Code!");
        }
        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(),
                request.getUserId(),
                couponCode,
                CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);

        //填充coupon对象的CouponTemplateSDK，一定要在放入缓存之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());
        //放入缓存
        redisService.addCouponToCache(
                request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode()
        );
        return newCoupon;
    }

    /**
     * 结算（核销）优惠券
     *
     * @param info
     * @return
     * @throws CouponException
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        return null;
    }
}
