package com.jhh.dc.loan.task;

import com.jhh.dc.loan.api.black.RiskBlackService;
import com.jhh.dc.loan.common.util.DateUtil;
import com.jhh.dc.loan.dao.BorrowListMapper;
import com.jhh.dc.loan.entity.app_vo.BorrowVO;
import com.jhh.dc.loan.service.PayCheckService;
import com.jhh.dc.loan.service.TimerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 重复放款检测
 */
@Component
@Slf4j
public class PayCheckTask {

    @Autowired
    PayCheckService payCheckService;

    /**
     * 每两个小时执行一次检测
     */
    @Scheduled(cron = "0 0 */2 * * ?")
    public void black() {
        log.info("【重复放款合同检测】定时任务开始。。。。");
        payCheckService.payCheck();
        log.info("【重复放款合同检测】定时任务结束。。。。");
    }
}
