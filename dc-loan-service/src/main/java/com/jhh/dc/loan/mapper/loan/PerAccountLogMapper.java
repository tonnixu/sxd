package com.jhh.dc.loan.mapper.loan;

import java.util.List;

import com.jhh.dc.loan.entity.loan.PerAccountLog;

public interface PerAccountLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PerAccountLog record);

    int insertSelective(PerAccountLog record);

    PerAccountLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PerAccountLog record);

    int updateByPrimaryKey(PerAccountLog record);
    
    List<PerAccountLog> getPerAccountLog(String userId, int start, int pageSize);
}