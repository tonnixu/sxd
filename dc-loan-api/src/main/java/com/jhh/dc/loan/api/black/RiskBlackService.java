package com.jhh.dc.loan.api.black;

import com.jhh.dc.loan.entity.app.NoteResult;


/**
 * 为清结算提供一个拉黑的dubbo服务接口
 */
public interface RiskBlackService {

    /**
     * 拉黑
     */
    NoteResult black(String phone, String cardNum, String personName, Integer perId);
}
