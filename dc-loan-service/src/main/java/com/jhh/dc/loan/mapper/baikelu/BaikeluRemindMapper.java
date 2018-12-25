package com.jhh.dc.loan.mapper.baikelu;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;

import com.jhh.dc.loan.entity.baikelu.BaikeluRemind;

public interface BaikeluRemindMapper extends Mapper<BaikeluRemind> {
    public void insertBaikeluRemindList(List<BaikeluRemind> list);
    public void updateBaikeluRemindList(List<BaikeluRemind> list);
}