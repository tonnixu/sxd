package com.jhh.dc.loan.api.channel;

import java.util.List;

import com.jhh.dc.loan.api.entity.ResponseDo;

/**
 * 2018/3/30.
 */
public interface AgentStateService {

    /**
     * 查询订单的支付状态
     * @param serNO
     * @return ResponseDo
     */
    ResponseDo state(String serNO) throws Exception;

}
