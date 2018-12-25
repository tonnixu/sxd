package com.jhh.dc.loan.api.refund;

import com.jhh.dc.loan.api.entity.ResponseDo;

public interface RefundService {
    /**
     * 退款更新订单流水
     *
     * @param serialNo 订单号
     */
    ResponseDo<?> refundState(String serialNo);

}
