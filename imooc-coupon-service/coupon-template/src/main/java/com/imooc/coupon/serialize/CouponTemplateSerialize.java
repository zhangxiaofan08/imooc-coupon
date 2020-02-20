package com.imooc.coupon.serialize;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.imooc.coupon.entity.CouponTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 优惠券模版实体类自定义序列化器
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 15:33
 */
public class CouponTemplateSerialize extends JsonSerializer<CouponTemplate> {
    @Override
    public void serialize(CouponTemplate couponTemplate, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        //开始序列化对象
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", couponTemplate.getId().toString());
        jsonGenerator.writeStringField("name", couponTemplate.getName());
        jsonGenerator.writeStringField("logo", couponTemplate.getLogo());
        jsonGenerator.writeStringField("desc", couponTemplate.getDesc());
        jsonGenerator.writeStringField("category", couponTemplate.getCategory().getDescription());
        jsonGenerator.writeStringField("productLine", couponTemplate.getProductLine().getDescription());
        jsonGenerator.writeStringField("count", couponTemplate.getCount().toString());
        jsonGenerator.writeStringField("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(couponTemplate.getCreateTime()));
        jsonGenerator.writeStringField("userId", couponTemplate.getUserId().toString());
        jsonGenerator.writeStringField("key", couponTemplate.getKey() + String.format("%04d", couponTemplate.getId()));
        jsonGenerator.writeStringField("target", couponTemplate.getTarget().getDescribtion());
        jsonGenerator.writeStringField("rule", JSON.toJSONString(couponTemplate.getRule()));

        //结束序列化对象
        jsonGenerator.writeEndObject();
    }
}
