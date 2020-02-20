package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.ITemplateBaseService;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 优惠券模版基础服务接口实现
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 21:54
 */
@Slf4j
@Service
public class TemplateBaseServiceimpl implements ITemplateBaseService {

    private final CouponTemplateDao templateDao;

    @Autowired
    public TemplateBaseServiceimpl(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    /**
     * 根据优惠券模版id获取优惠券模版信息
     *
     * @param id
     * @return
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        Optional<CouponTemplate> template = templateDao.findById(id);
        if (!template.isPresent()) {
            throw new CouponException("Template Is Not Exist: " + id);
        }
        return template.get();
    }

    /**
     * 查找所有可用的优惠券模版
     *
     * @return
     */
    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {

        List<CouponTemplate> templateList = templateDao.findAllByAvailableAndExpired(true, false);

        return templateList.stream().map(this::template2SDK).collect(Collectors.toList());
    }

    /**
     * 获取模版 ids 到 CouponTemplateSDK 的映射
     *
     * @param ids
     * @return Map<key : 模版id, value : CouponTemplateSDK>
     */
    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {

        List<CouponTemplate> templateList = templateDao.findAllById(ids);

        return templateList.stream().map(this::template2SDK)
                .collect(Collectors.toMap(CouponTemplateSDK::getId, Function.identity()));
    }

    /**
     * 将 CouponTemplate 转换为 CouponTemplateSDK
     * @param template
     * @return
     */
    private CouponTemplateSDK template2SDK(CouponTemplate template) {
        return new CouponTemplateSDK(
                template.getId(),
                template.getName(),
                template.getLogo(),
                template.getDesc(),
                template.getCategory().getCode(),
                template.getProductLine().getCode(),
                template.getKey(),  // 并不是拼装好的 Template Key
                template.getTarget().getCode(),
                template.getRule()
        );
    }
}
