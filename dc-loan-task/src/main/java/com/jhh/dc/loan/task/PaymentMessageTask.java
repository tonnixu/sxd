package com.jhh.dc.loan.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jhh.dc.loan.service.FinanceService;
import com.jhh.dc.loan.service.TimerService;

/**
 * Created by wanzezhong on 2017/8/22.
 * 还款消息定时
 */
@Component
public class PaymentMessageTask {

    @Autowired
    private TimerService timerService;

    @Autowired
    private FinanceService financeService;


    /**
     * 每天上午10点对当天和明天要还款的人进行短信提醒
     */
    @Scheduled(cron = "0 0 10 * * ? ")
//    @Scheduled(cron = "0 0/5 * * * ? ")
    public void smsAlert() {
        timerService.smsAlert();
    }

    /**
     * 每天定时给逾期第7天发短信
     */
   // @Scheduled(cron = "0 0 12 * * ? ")
//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void smsOverdue() {
        System.out.println("smsOverdue strat");
        timerService.smsOverdue();

    }

    /**
     * 每天定时给资金管理发邮件 每天早上9点发前一天的放款数据
     */
    @Scheduled(cron = "0 5 09 * * ? ")
    public void sendMoneyManagement() {
        System.out.println("财务报表定时任务");
        financeService.sendPayData();

    }

    /**
     * 给财务发送还款数据
     */
    @Scheduled(cron = "0 0 09 * * ? ")
    public void sendDataToFinance() {
        financeService.sendRepayData();
    }

}
