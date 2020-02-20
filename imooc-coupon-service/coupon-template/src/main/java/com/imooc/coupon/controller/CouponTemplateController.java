package com.imooc.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.service.ITemplateBaseService;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模版相关功能控制器
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 23:57
 */
@Slf4j
@RestController
public class CouponTemplateController {

    /**
     * 构建优惠券模版服务
     */
    private final IBuildTemplateService buildTemplateService;
    /**
     * 优惠券模版基础服务
     */
    private final ITemplateBaseService templateBaseService;

    @Autowired
    public CouponTemplateController(IBuildTemplateService buildTemplateService, ITemplateBaseService templateBaseService) {
        this.buildTemplateService = buildTemplateService;
        this.templateBaseService = templateBaseService;
    }

    /**
     * 构建优惠券模版
     * localhost:7001/coupon-template/template/build
     * localhost:9000/imooc/coupon-template/template/build
     *
     * @param request
     * @return
     */
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException {
        log.info("Build Template: {}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    /**
     * 构建优惠券模版详情
     * localhost:7001/coupon-template/template/info?id=1
     * localhost:9000/imooc/coupon-template/template/info?id=1
     *
     * @param id
     * @return
     */
    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException {
        log.info("Build Template Info For: {}", id);
        return templateBaseService.buildTemplateInfo(id);
    }

    /**
     * 查找所有可用的优惠券模版
     * localhost:7001/coupon-template/template/sdk/all
     * localhost:9000/imooc/coupon-template/template/sdk/all
     * @return
     */
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        log.info("Find All Usable Template.");
        return templateBaseService.findAllUsableTemplate();
    }

    /**
     * 获取模版ids到CouponTemplateSDK的映射
     * localhost:7001/coupon-template/template/sdk/infos?ids=1,2,3
     * localhost:9000/imooc/coupon-template/template/sdk/infos?ids=1,2,3
     * @param ids
     * @return
     */
    @GetMapping("/template/sdk/infos")
    public Map<Integer, CouponTemplateSDK> findIds2SDK(@RequestParam("ids") Collection<Integer> ids) {
        log.info("FindIds2TemplateSDK: {}", JSON.toJSONString(ids));
        return templateBaseService.findIds2TemplateSDK(ids);
    }
}
