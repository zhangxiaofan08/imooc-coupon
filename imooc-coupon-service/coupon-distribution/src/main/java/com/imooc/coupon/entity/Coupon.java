package com.imooc.coupon.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.converter.CouponStatusConverter;
import com.imooc.coupon.serialization.CouponSerialize;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * 优惠券（用户领取的优惠券记录）实体表
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 11:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)  // jpa审计功能，对列属性实现自动填充
@Table(name = "coupon")
@JsonSerialize(using = CouponSerialize.class)
public class Coupon {
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * 关联优惠券模版的主键（逻辑外键）
     */
    @Column(name = "template_id", nullable = false)
    private Integer templateId;
    /**
     * 领取用户
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 优惠券码
     */
    @Column(name = "coupon_code", nullable = false)
    private String couponCode;

    /**
     * 领取时间
     */
    @CreatedDate
    @Column(name = "assign_time", nullable = false)
    private Date assignTime;

    /**
     * 优惠券状态
     */
    @Convert(converter = CouponStatusConverter.class)
    @Column(name = "status", nullable = false)
    private CouponStatus status;

    /**
     * 用户优惠券对应的模版信息
     */
    @Transient
    private CouponTemplateSDK templateSDK;

    /**
     * 返回一个无效的coupon对象
     * @return
     */
    public static Coupon invalidCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(-1);
        return coupon;
    }

    public Coupon(Integer templateId, Long userId, String couponCode, CouponStatus status) {
        this.templateId = templateId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.status = status;
    }
}
