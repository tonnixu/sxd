package com.jhh.dc.loan.api.channel;


import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.capital.AgentRefundRequest;

/**
 * 退款服务
 */
public interface AgentRefundService {

    /**
     * 退款
     */
    ResponseDo<?> refund(AgentRefundRequest refund);

}
