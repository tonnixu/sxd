package com.jhh.dc.loan.api.channel;

import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.pay.driver.pojo.PayResponse;

import java.util.List;

/**
 * 2018/6/5.
 */
public interface TradeBatchStateService {

    ResponseDo<PayResponse> batchState(List<String> loanOrder);
}
