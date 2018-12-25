package com.jhh.dc.loan.mapper.manager;

import java.util.List;

import com.jhh.dc.loan.entity.manager.Review;
import com.jhh.dc.loan.entity.manager_vo.ReviewVo;

public interface ReviewMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Review record);

    int insertSelective(Review record);

    Review selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Review record);

    int updateByPrimaryKey(Review record);
     
    int blackListStatus(String brro_id);
    //获取审核表总数
    Integer reviewSum();
    List<ReviewVo> getReviewVoBlackList(int himid);

    //查询合同是否有审核人
    int selectReview(Integer borrId);

    Review selectByBorrId(Integer borrId);

    List<Review> selectReviewByBorrIdAndReviewType(Review review);
    
}