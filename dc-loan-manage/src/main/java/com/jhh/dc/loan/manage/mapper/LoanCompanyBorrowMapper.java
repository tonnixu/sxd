package com.jhh.dc.loan.manage.mapper;


import tk.mybatis.mapper.common.Mapper;

import java.util.List;

import com.jhh.dc.loan.entity.manager.LoanCompanyBorrow;

public interface LoanCompanyBorrowMapper extends Mapper<LoanCompanyBorrow> {

    /**
     * 批量更新
     * @param updateCompanyBorrow
     * @return
     */
    int batchUpdate(List<LoanCompanyBorrow> updateCompanyBorrow);

    /**
     * 批量插入
     * @param insertCompanyBorrow
     * @return
     */
    int batchInsert(List<LoanCompanyBorrow> insertCompanyBorrow);
}
