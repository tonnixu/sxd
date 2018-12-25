package com.jhh.dc.loan.dao;

import com.jhh.dc.loan.entity.loan.BorrowDeductions;

import tk.mybatis.mapper.common.Mapper;

public interface BorrowDeductionsMapper extends Mapper<BorrowDeductions> {
    BorrowDeductions selectByBorrId(Integer borrId);
}
