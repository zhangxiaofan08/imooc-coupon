package com.imooc.coupon.dao;

import com.imooc.coupon.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * CouponTemplate Dao 接口定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 15:53
 */
public interface CouponTemplateDao extends JpaRepository<CouponTemplate, Integer> {
    /**
     * 根据模版名称查询模版
     * 相当于 where name = 'name'
     * @param name
     * @return
     */
    CouponTemplate findByName(String name);

    /**
     * 根据available和expired标记查找模版记录
     * 相当于 where available = 。。。 and expired = 。。。
     * @param available
     * @param expired
     * @return
     */
    List<CouponTemplate> findAllByAvailableAndExpired(Boolean available, Boolean expired);

    /**
     * 根据expired标记查找模版记录
     * 相当于 where expired = ...
     * @param expired
     * @return
     */
    List<CouponTemplate> findAllByExpired(Boolean expired);
}
