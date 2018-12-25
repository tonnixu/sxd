package com.jhh.dc.loan.mapper.contract;

import com.jhh.dc.loan.entity.contract.Contract;

import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

public interface ContractMapper extends Mapper<Contract> {
    int insertContract(Contract contract);

    String getContractUrl(String borrId);

    String selectByBorrowNum(String borrowNum);

    Map<String, String> selectContractByBorrId(Integer borrId);
}