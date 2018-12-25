package com.jhh.dc.loan.manage.mapper;


import java.util.List;

import com.jhh.dc.loan.entity.manager.SmsTemplate;

public interface SmsTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsTemplate record);

    SmsTemplate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SmsTemplate record);

    int updateByPrimaryKey(SmsTemplate record);
    
    List<SmsTemplate> getAllSmsTemplateList();
}