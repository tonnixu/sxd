package com.jhh.dc.loan.mapper.sms;

import org.apache.ibatis.annotations.Param;

import com.jhh.dc.loan.entity.manager.SmsTemplate;

/**
 * 短信模板
 */
public interface SmsTemplateMapper {

    /**
     *  获取对应短信模板
     * @param templateSeq
     * @return
     */
    public SmsTemplate getSmsTemplate(@Param("templateSeq") int templateSeq);
}
