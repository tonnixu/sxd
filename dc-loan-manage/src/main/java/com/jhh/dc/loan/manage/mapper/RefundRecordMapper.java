package com.jhh.dc.loan.manage.mapper;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.entity.refund.RefundReview;
import com.jhh.dc.loan.entity.refund.RefundReviewVo;

public interface RefundRecordMapper extends Mapper<RefundReview> {
    /**
     * 退款流水
     * @returnF
     */
    List getRefundRecord();

}