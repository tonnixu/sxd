package com.jhh.dc.loan.api.channel;

import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.dc.loan.api.entity.capital.AgentDeductRequest;

/**
 *  代扣
 */
public interface AgentDeductService {
    
     ResponseDo<?> deduct(AgentDeductRequest request);

     ResponseDo<?> deductBatch(AgentDeductBatchRequest requests);

    /**
     *  到期日批量代扣 定时任务提供
     */
    void batchExpireDeduct();


}
