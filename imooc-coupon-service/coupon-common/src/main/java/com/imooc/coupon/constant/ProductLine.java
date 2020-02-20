package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 产品线枚举
 * @AUTHOR zhangxf
 * @CREATE 2020-02-11 22:52
 */
@Getter
@AllArgsConstructor
public enum ProductLine {

    DAMAO("大猫", 1),
    DABAO("大宝", 2);
    /**
     * 产品线描述
     */
    private String description;
    /**
     * 产品线编码
     */
    private Integer code;

    public static ProductLine of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }
}
