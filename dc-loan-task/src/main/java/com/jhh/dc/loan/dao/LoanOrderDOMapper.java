package com.jhh.dc.loan.dao;

import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.loan.BorrowDeductions;
import com.jhh.dc.loan.model.LoanOrderDO;
import com.jhh.dc.loan.model.LoanOrderDOExample;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface LoanOrderDOMapper {
    int countByExample(LoanOrderDOExample example);

    int deleteByExample(LoanOrderDOExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LoanOrderDO record);

    int insertSelective(LoanOrderDO record);

    List<LoanOrderDO> selectByExample(LoanOrderDOExample example);

    LoanOrderDO selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") LoanOrderDO record, @Param("example") LoanOrderDOExample example);

    int updateByExample(@Param("record") LoanOrderDO record, @Param("example") LoanOrderDOExample example);

    int updateByPrimaryKeySelective(LoanOrderDO record);

    int updateByPrimaryKey(LoanOrderDO record);

    /**
     *
     /**
     * 查询逾期未还的合同的一天内的最近一笔订单状态和订单失败的原因
     *
     * @return
     */
    List<BorrowDeductions> selectOverDueOrderStatus();

    List<BorrowDeductions> selectBankCardError();

    List<LoanOrderDO> selectOrderByDeduct();

    List<LoanOrderDO> selectOrderByBatchDeduct();

    List<LinkedHashMap> getMultiplePayByBorrowList();
}