package com.imooc.coupon.vo;

import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户优惠券的分类，根据优惠券状态
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 11:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {
    /**
     * 可以使用的
     */
    private List<Coupon> usable;

    /**
     * 已使用的
     */
    private List<Coupon> used;

    /**
     * 已过期的
     */
    private List<Coupon> expired;

    /**
     * 对当前的优惠券进行分类
     * @param couponList
     * @return
     */
    public static CouponClassify classify(List<Coupon> couponList) {
        List<Coupon> usable = new ArrayList<>(couponList.size());
        List<Coupon> used = new ArrayList<>(couponList.size());
        List<Coupon> expired = new ArrayList<>(couponList.size());

        couponList.forEach(coupon -> {
            //判断优惠券是否过期
            boolean isTimeExpire;
            long currentTime = new Date().getTime();

            if (coupon.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR.getCode())) {
                isTimeExpire = coupon.getTemplateSDK().getRule().getExpiration().getDeadline() <= currentTime;
            } else {
                isTimeExpire = DateUtils.addDays(coupon.getAssignTime(),
                        coupon.getTemplateSDK().getRule().getExpiration().getGap()).getTime() <= currentTime;
            }

            if (coupon.getStatus() == CouponStatus.USED) {
                used.add(coupon);
            } else if (coupon.getStatus() == CouponStatus.EXPIRED || isTimeExpire) {
                expired.add(coupon);
            } else {
                usable.add(coupon);
            }
        });

        return new CouponClassify(usable, used, expired);
    }
}
