package com.jhh.dc.loan.manage.mapper;

import com.jhh.dc.loan.manage.entity.RobotQuestion;

import tk.mybatis.mapper.common.Mapper;

public interface RobotQuestionMapper extends Mapper<RobotQuestion> {

    int insertRobotQuestion(RobotQuestion robotQuestion);

}
