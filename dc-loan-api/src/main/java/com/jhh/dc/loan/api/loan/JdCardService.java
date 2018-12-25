package com.jhh.dc.loan.api.loan;

import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.entity.app_vo.JdCardDetailVO;

/**
 * 2018/7/30.
 */
public interface JdCardService {

    ResponseDo<JdCardDetailVO> getCardDetailByCardId(String phone,String borrNum);
}
