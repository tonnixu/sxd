package com.jhh.dc.loan.api.channel;

import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.capital.AgentPayRequest;

/**
 * 2018/3/30.
 */
public interface AgentPayService {
    
    ResponseDo<?> pay(AgentPayRequest pay);

    ResponseDo<?> payCard(AgentPayRequest pay);
}
