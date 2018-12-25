package com.jhh.dc.loan.api.loan;

import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.entity.loan_vo.ResponseVo;

/**
 * 审核管理dubbo服务
 */
public interface ReviewManageService{
    /**
     * 审核放款
     */
    ResponseVo pay(Integer borrId, String userNum, String payChannel);

    /**
     * 风控转件
     */
    ResponseVo transfer(String borrIds, String userNum);

}
