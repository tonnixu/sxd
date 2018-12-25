package com.jhh.dc.loan.manage.mapper;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.entity.app.Bank;
import com.jhh.dc.loan.entity.app.BankVo;

public interface BankInfoMapper extends Mapper<Bank> {

    BankVo selectBankInfos(Map<String, Object> map);


    List<BankVo> selectBankInfosByPerId(Map<String, Object> map);

    /**
     * 查询用户主卡
     * @param userId
     * @return
     */
    BankVo selectMainBankByUserId(Integer userId);

    Bank selectByBankNumAndStatus(String bankNum);
}
