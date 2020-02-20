package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IAsyncService;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 构建优惠券模版接口实现
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 21:10
 */
@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {
    //异步服务
    private final IAsyncService asyncService;

    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }

    /**
     * 创建优惠券模版
     *
     * @param request {@link TemplateRequest} 模版信息请求对象
     * @return {@link CouponTemplate} 优惠券模版实体
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        //参数合法性校验
        if (!request.validate()) {
            throw new CouponException("BuildTemplate Param Is Not Valid!");
        }
        //判断是否存在同名的优惠券模版
        if (null != templateDao.findByName(request.getName())) {
            throw new CouponException("Exist Same Name Template!");
        }
        //构造 CouponTemplate 并保存到数据库中
        CouponTemplate template = request2Template(request);
        template = templateDao.save(template);
        //根据优惠券模版异步生成优惠券码
        asyncService.asyncConstructCouponByTemplate(template);

        return template;
    }

    /**
     * 将 TemplateRequest 转换为 CouponTemplate
     * @param request
     * @return
     */
    private CouponTemplate request2Template(TemplateRequest request) {
        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
