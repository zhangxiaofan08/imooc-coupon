package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用响应对象定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-11 10:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public CommonResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
