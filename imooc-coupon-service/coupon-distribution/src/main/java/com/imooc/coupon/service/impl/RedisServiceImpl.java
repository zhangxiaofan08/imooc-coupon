package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis相关操作服务接口实现
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 21:10
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据 suerId 和 状态 找到缓存的优惠券列表数据
     *
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     */
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache: {}, {}", userId, status);

        String redisKey = status2RedisKey(status, userId);

        List<String> couponStrList = redisTemplate.opsForHash().values(redisKey).stream()
                .map(obj -> Objects.toString(obj, null))
                .collect(Collectors.toList());

        // 没有获取到就保存空的优惠券列表到缓存中，避免缓存穿透
        if (CollectionUtils.isEmpty(couponStrList)) {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrList.stream()
                .map(couponStr -> JSON.parseObject(couponStr, Coupon.class))
                .collect(Collectors.toList());
    }

    /**
     * 保存空的优惠券列表到缓存中
     * 目的是避免缓存穿透
     *
     * @param userId
     * @param statusList
     */
    @Override
    @SuppressWarnings("all")
    public void saveEmptyCouponListToCache(Long userId, List<Integer> statusList) {
        log.info("Save Empty List To Cache For User: {}, Status: {}", userId, JSON.toJSONString(statusList));

        // key 是 coupon_id，value是序列化的coupon
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));

        // 用户优惠券    KV结构
        // K: status -> redisKey
        // V: {coupon_id: 序列化的 Coupon}

        // 使用SessionCallback把数据命令放入到Redis的piplinezhong
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                statusList.forEach(status -> {
                    String redisKey = status2RedisKey(status, userId);
                    redisOperations.opsForHash().putAll(redisKey, invalidCouponMap);

                });
                return null;
            }
        };

        log.info("Pipline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    /**
     * 尝试从cache中获取优惠券码
     *
     * @param templateId 优惠券模版主键
     * @return 优惠券码
     */
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());

        // 因为优惠券码不存在顺序关系，左边pop或右边pop都没有影响
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);

        log.info("Acquire Coupon Code: {}, {}, {}", templateId, redisKey, couponCode);
        return couponCode;
    }

    /**
     * 将优惠券保存到cache中
     *
     * @param userId
     * @param couponList
     * @param status
     * @return 保存成功的个数
     * @throws CouponException
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> couponList, Integer status) throws CouponException {
        log.info("Add Coupon To Cache: {}, {}, {}", userId, JSON.toJSONString(couponList), status);

        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus) {
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, couponList);
                break;
            case USABLE:
                result = addCouponToCackeForUsable(userId, couponList);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, couponList);
                break;
        }
        return null;
    }

    /**
     * 新增加优惠券到Cache中
     *
     * @param userId
     * @param couponList
     * @return
     */
    private Integer addCouponToCackeForUsable(Long userId, List<Coupon> couponList) {

        // 如果status是USABLE，代表是新增加的优惠券，只会影响一个Cache：USER_COUPON_USABLE_
        log.info("Add Coupon To Cache For Usable.");

        Map<String, String> needCachedObject = new HashMap<>(couponList.size());
        couponList.forEach(coupon ->
                // key 是 coupon_id，value是序列化的coupon
                needCachedObject.put(
                        coupon.getId().toString(),
                        JSON.toJSONString(coupon)
                ));
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);

        redisTemplate.opsForHash().putAll(redisKey, needCachedObject);
        log.info("Add {} Coupons To Cache: {}, {}", needCachedObject.size(), userId, redisKey);

        redisTemplate.expire(redisKey, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);

        return needCachedObject.size();
    }

    /**
     * 将已使用的优惠券加入到Cache中
     *
     * @param userId
     * @param couponList
     * @return
     */
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> couponList) throws CouponException {
        // 如果 status 是 USED，代表用户操作是使用当前的优惠券，影响到两个 Cache -- USABLE 、USED
        log.debug("Add Coupon To Cache For Used.");

        Map<String, String> needCachedForUsed = new HashMap<>(couponList.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);

        //获取当前用户可用的优惠券
        List<Coupon> currentUsableCouponList = getCachedCoupons(userId, CouponStatus.USABLE.getCode());

        // 可用的优惠券数量 一定大于 将已使用的
        assert currentUsableCouponList.size() > couponList.size();

        couponList.forEach(coupon -> needCachedForUsed.put(coupon.getId().toString(), JSON.toJSONString(coupon)));

        // 校验当前的优惠券参数是否与 Cached 中的匹配
        List<Integer> currentUsableIdList = currentUsableCouponList.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
        List<Integer> paramUsedIdList = couponList.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());

        if (!CollectionUtils.isSubCollection(paramUsedIdList, currentUsableCouponList)) {
            log.error("CurCoupons Is Not Equal To Cache: {}, {}, {}", userId,
                    JSON.toJSONString(paramUsedIdList),
                    JSON.toJSONString(currentUsableCouponList));
            throw new CouponException("CurCoupons Is Not Equal To Cache.");
        }

        List<String> needCleanKey = paramUsedIdList.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //1、已使用的优惠券 Cache 缓存添加
                redisOperations.opsForHash().putAll(redisKeyForUsed, needCachedForUsed);

                //2、可用的优惠券 Cache 需要清理
                redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());

                //3、重置过期时间
                redisOperations.expire(redisKeyForUsable, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);

                redisOperations.expire(redisKeyForUsed, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);

                return null;
            }
        };

        log.info("Pipline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));

        return couponList.size();
    }

    /**
     * 将优惠券加入到Cache中
     *
     * @param userId
     * @param couponList
     * @return
     */
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> couponList) throws CouponException {
        // status 是 EXPIRED，代表是已有的优惠券过期了，影响到两个Cache：USABLE、EXPIRED
        log.debug("Add Coupon To Cache For Expired.");

        //最终需要保存的Cache
        Map<String, String> needCachedForExpired = new HashMap<>(couponList.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);

        List<Coupon> currentUsableCouponList = getCachedCoupons(userId, CouponStatus.USABLE.getCode());
        List<Coupon> currentExpiredCouponList = getCachedCoupons(userId, CouponStatus.EXPIRED.getCode());

        //当前可用的优惠券个数一定是大于1de
        assert currentUsableCouponList.size() > couponList.size();

        couponList.forEach(coupon -> needCachedForExpired.put(coupon.getId().toString(), JSON.toJSONString(coupon)));

        //校验当前的优惠券参数是否与 Cache 中的匹配
        List<Integer> currentUsableIdList = currentUsableCouponList.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
        List<Integer> paramIdList = couponList.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isSubCollection(paramIdList, currentUsableIdList)) {
            log.error("Current Coupon List Is Not Equal To Cache: {}, {}, {}", userId,
                    JSON.toJSONString(paramIdList),
                    JSON.toJSONString(currentUsableIdList));
            throw new CouponException("Current Coupon List Is Not Equal To Cache");

        }

        List<String> needCleanKey = paramIdList.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //1、已过期的优惠券cache缓存
                redisOperations.opsForHash().putAll(redisKeyForExpired, needCachedForExpired);
                //2、可用的优惠券cache需要清理
                redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey);
                //3、重置过期时间
                redisOperations.expire(redisKeyForUsable, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForExpired, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return couponList.size();
    }

    /**
     * 根据 status 获取到对应的 Redis Key
     *
     * @param status
     * @param userId
     * @return
     */
    private String status2RedisKey(Integer status, Long userId) {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus) {
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;
    }

    /**
     * 获取一个随机的过期时间（秒）
     *
     * @param min 最小的小时数
     * @param max 最大的小时数
     * @return
     */
    private Long getRandomExpirationTime(Integer min, Integer max) {
        return RandomUtils.nextLong(min * 60 * 60, max * 60 * 60);
    }

}
