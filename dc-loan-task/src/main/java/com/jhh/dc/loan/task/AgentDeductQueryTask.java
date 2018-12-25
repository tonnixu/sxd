package com.jhh.dc.loan.task;

import com.jhh.dc.loan.api.channel.AgentBatchStateService;
import com.jhh.dc.loan.api.channel.AgentChannelService;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.dc.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.dc.loan.dao.BorrowListMapper;
import com.jhh.dc.loan.dao.CodeValueMapper;
import com.jhh.dc.loan.dao.LoanOrderDOMapper;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.manager.CodeValue;
import com.jhh.dc.loan.model.LoanOrderDO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright © 2018 上海金互行金融服务有限公司. All rights reserved. *
 *
 * @author luolong
 * @date 2018-1-29
 */
@Component
@Slf4j
@SuppressWarnings("SpringJavaAutowiringInspection")
public class AgentDeductQueryTask {

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private AgentChannelService agentChannelService;

    @Autowired
    private AgentBatchStateService batchState;

    @Value("${dbcp.url}")
    private String url;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void doAgentpayQuery() {
        log.info("\n代扣主动查询进来了" + url);
        doIt();
        log.info("\n代扣主动查询结束了");
    }

    private void doIt() {
        List<LoanOrderDO> loanOrderDOList = loanOrderDOMapper.selectOrderByDeduct();
        log.info("\n获取需要查询状态的代扣单子开始:在处理的代扣单子数量：" + (loanOrderDOList == null ? 0 : loanOrderDOList.size()));
        try {
            if (loanOrderDOList != null) {
                for (LoanOrderDO loanOrderDO : loanOrderDOList) {
                    log.info("\n当前正在处理的代扣单子：{}", loanOrderDO.getSerialNo());
                    if (agentChannelService == null) {
                        log.info("agentDeductService 为空");
                    } else {
                        log.info("agentDeductService 不为空");
                    }
                    ResponseDo resut = agentChannelService.state(loanOrderDO.getSerialNo());
                    log.info("单子 返回result {}", resut);
                }
            }
        } catch (Exception e) {
            log.error("查询代扣订单状态定时器出错", e);
        }
        log.info("\n获取需要查询状态的代扣单子结束");
    }

    @Scheduled(cron = "0 0/3 * * * ?")
    public void batchDeduct() throws Exception {
        log.info("\n代扣主动查询进来了" + url);
        List<LoanOrderDO> loanOrderDOs = loanOrderDOMapper.selectOrderByBatchDeduct();
        List<String> list = loanOrderDOs.stream().map(LoanOrderDO::getSerialNo).collect(Collectors.toList());
        if (list.size() > 0) {
            batchState.batchState(list);
        }
        log.info("\n代扣主动查询结束了");
    }


    /**
     * 到期日定时任务 每日晚十点
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void batchDeductByExpire() {
        log.info("每日定时查询到期用户并批量代扣开始-------------------");
        //判断批扣开关是否打开
        CodeValue batchSwitch = codeValueMapper.getCodeValueByCode("batchDeduct_switch");
        if (batchSwitch != null && "1".equals(batchSwitch.getCodeCode())){
            agentChannelService.batchExpireDeduct();
        }
    }
}
