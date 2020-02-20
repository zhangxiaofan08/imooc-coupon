package com.imooc.coupon.schedule;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时清理已过期的优惠券模版
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 22:15
 */
@Slf4j
@Component
public class ScheduleTask {

    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduleTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    /**
     * 下线已过期的优惠券模版
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlineCouponTemplate() {
        log.info("Start To Expire CouponTemplate.");

        // 查询所有未过期的
        List<CouponTemplate> templateList = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templateList)) {
            log.info("Done To Expire CouponTemplate.");
            return;
        }

        Date curDate = new Date();
        List<CouponTemplate> expiredTemplateList = new ArrayList<>(templateList.size());

        templateList.forEach(template -> {
            // 根据优惠券模版规则中的"过期规则"校验模版是否过期
            TemplateRule rule = template.getRule();
            if (rule.getExpiration().getDeadline() < curDate.getTime()) {
                template.setExpired(true);
                expiredTemplateList.add(template);
            }
        });

        if (CollectionUtils.isNotEmpty(expiredTemplateList)) {
            log.info("Expired CouponTemplate Num: {}",
                    templateDao.saveAll(expiredTemplateList));
        }

        log.info("Done To Expire CouponTemplate.");
    }
}
