package com.jhh.dc.loan.manage.mapper;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.entity.manager.Review;

public interface ReviewMapper extends Mapper<Review> {

	List manualAuditReport(Map map);

	List getauditsforUser(Map map);

	List getManuallyReview(Map map);

}