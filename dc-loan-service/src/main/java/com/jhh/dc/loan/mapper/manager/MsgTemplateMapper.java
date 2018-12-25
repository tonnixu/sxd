package com.jhh.dc.loan.mapper.manager;

import java.util.List;

import com.jhh.dc.loan.entity.manager.MsgTemplate;
import com.jhh.dc.loan.entity.manager_vo.MsgTemplateVo;

public interface MsgTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MsgTemplate record);

    int insertSelective(MsgTemplate record);

    MsgTemplate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MsgTemplate record);

    int updateByPrimaryKeyWithBLOBs(MsgTemplate record);

    int updateByPrimaryKey(MsgTemplate record);
    
    List<MsgTemplateVo> getAllMsgTemplateList();
}