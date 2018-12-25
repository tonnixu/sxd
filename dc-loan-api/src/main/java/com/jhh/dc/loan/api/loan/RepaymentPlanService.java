package com.jhh.dc.loan.api.loan;

import java.util.List;

import com.jhh.dc.loan.entity.manager.RepaymentPlan;

/**
 * Created by chenchao on 2018/1/10.
 */
public interface RepaymentPlanService {

    public List getRepaymentTermPlan(String borrowId);

    List<RepaymentPlan> getOverdueRepaymentPlan(String borrId);
}
