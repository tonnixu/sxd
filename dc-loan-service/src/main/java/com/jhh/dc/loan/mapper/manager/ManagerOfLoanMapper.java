package com.jhh.dc.loan.mapper.manager;

import java.util.List;

import com.jhh.dc.loan.entity.app.Image;
import com.jhh.dc.loan.entity.app.Reviewers;
import com.jhh.dc.loan.entity.manager.Review;
import com.jhh.dc.loan.entity.manager_vo.*;

public interface ManagerOfLoanMapper {
    

    int updateByPrimaryKeySelective(ReqBackPhoneCheckVo record);
    
    List<Reviewers> selectRiewerList(String status);
    List<Reviewers> selectRiewerListAll();
    
    PrivateVo selectUserPrivateVo(int perid);
    
    List<LoanInfoVo>  selectLoanInfoPrivateVo(int himid);
    
    List<BankInfoVo> selectBankInfoVo(int himid);
    
    int personCheckMessage(Review record);
    int transferPersonCheck(Review record);
    
    CardPicInfoVo getCardPicById(int himid);
    List<Image> PicBatchVo(int pageIndex, int pageSize);
}