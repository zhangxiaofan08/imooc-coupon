package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品信息
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 16:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsInfo {
    /**
     * 商品类型
     * {@link com.imooc.coupon.constant.GoodsType}
     */
    private Integer type;

    /**
     * 商品价格
     */
    private Double price;

    /**
     * 商品数量
     */
    private Integer count;

    // TODO: 2020-02-14
}
