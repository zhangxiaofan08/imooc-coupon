package com.imooc.coupon.feign;

import com.imooc.coupon.feign.hystrix.TemplateClientHystrix;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.CouponTemplateSDK;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模版微服务 Feign 接口定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 10:46
 */
@FeignClient(value = "eureka-client-coupon-template", fallback = TemplateClientHystrix.class)
public interface TemplateClient {
    /**
     * 查找所有可用的优惠券模版
     *
     * @return
     */
    @RequestMapping(value = "/coupon-template/template/sdk/all", method = RequestMethod.GET)
    CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate();

    /**
     * 获取模版ids到CouponTemplateSDK的映射
     *
     * @return
     */
    @RequestMapping(value = "/coupon-template/template/sdk/infos", method = RequestMethod.GET)
    CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2SDK(@RequestParam("ids") Collection<Integer> ids);

}
