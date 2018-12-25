package com.jhh.dc.loan.manage.mapper;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.entity.manager.CollectorsRemark;

public interface CollectorsRemarkMapper extends Mapper<CollectorsRemark> {

    public List<CollectorsRemark> selectRemarkInfo(Map<String, Object> paramMap);
}
