package com.jhh.dc.loan.service.impl;

import com.jhh.dc.loan.api.channel.AgentChannelService;
import com.jhh.dc.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.dc.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.dc.loan.api.white.RiskWhiteService;
import com.jhh.dc.loan.dao.*;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.common.Constants;
import com.jhh.dc.loan.entity.loan.BorrowDeductions;
import com.jhh.dc.loan.entity.loan.CollectorsList;
import com.jhh.dc.loan.model.CollRecordData;
import com.jhh.dc.loan.model.OrderRobot;
import com.jhh.dc.loan.service.BorrListService;
import com.jhh.dc.loan.service.RobotService;
import com.jhh.dc.loan.common.util.Detect;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * 合同相关
 */
@Service @Setter
public class BorrListServiceImpl implements BorrListService {

    private static final Logger logger = LoggerFactory.getLogger(BorrListServiceImpl.class);

    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private CollectorsListMapper collectorsListMapper;
    @Autowired
    private AgentChannelService agentChannelService;
    @Autowired
    private RobotService robotService;
    @Autowired
    private BorrowDeductionsMapper borrowDeductionsMapper;
    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;
    @Autowired
    CardMapper cardMapper;
    @Autowired
    RiskWhiteService riskWhiteService;
    @Autowired
    OrderRobotMapper orderRobotMapper;

    @Override
    public Integer submenuTransfer() {
        //查询为分单订单
        List<CollectorsList> collectorsList = borrowListMapper.getCollectorsByOverdue();
        if(Detect.notEmpty(collectorsList)){
            //分配给特殊
            collectorsListMapper.batchInsertCollectorsList(collectorsList);

            Set set = new HashSet();
            for (CollectorsList collectors :collectorsList){
                set.add(collectors.getContractSysno());
            }
            //更新合同冗余字段
            BorrowList borrowList = new BorrowList();
            borrowList.setCollectionUser("A1131");
            Example blExample = new Example(BorrowList.class);
            blExample.createCriteria().andIn("id", set);
            return borrowListMapper.updateByExampleSelective(borrowList, blExample);
        }
        return 0;
    }

    @Override
    public void updateOverdueThree() {
        //查询逾期三天的合同（四期产品）
        List borrIdList = borrowListMapper.getOverdueThree();
        if(borrIdList != null && borrIdList.size() > 0){
            //查询逾期三天的合同用于插入历史记录表的信息
            List<CollRecordData> collRecorddataList = borrowListMapper.getCollectorsOverdueThree();
            //插入历史记录
            borrowListMapper.insertCollectorsRecode(collRecorddataList);
            //更新borrow_list表的催款人冗余字段，催款人设为杨艳
            borrowListMapper.updateOverdueThreeBorrow(borrIdList);
            //更新collection_list表，催款人设为杨艳
            borrowListMapper.updateOverdueThreeCollectors(borrIdList);
        }
    }

    @Override
    public void updateOverdueTwo() {
        //查询逾期两天的合同（一期产品）
        List borrIdList = borrowListMapper.getOverdueTwo();
        if(borrIdList != null && borrIdList.size() > 0){
            //查询逾期两天的合同用于插入历史记录表的信息
            List<CollRecordData> collRecorddataList = borrowListMapper.getCollectorsOverdueTow();
            //插入历史记录
            borrowListMapper.insertCollectorsRecode(collRecorddataList);
            //更新borrow_list表的催款人冗余字段，催款人设为杨艳
            borrowListMapper.updateOverdueThreeBorrow(borrIdList);
            //更新collection_list表，催款人设为杨艳
            borrowListMapper.updateOverdueThreeCollectors(borrIdList);
        }
    }

    @Override
    public void rejectAudit() {
        //自动拒绝人工审核订单
        borrowListMapper.rejectAudit();
    }

    @Override
    public void batchWithhold() {
        List<AgentDeductRequest> agentDeductRequests =  borrowListMapper.getBatchWithhold();
        if(Detect.notEmpty(agentDeductRequests)){
            AgentDeductBatchRequest agentDeductBatchRequest = new AgentDeductBatchRequest();
//            agentDeductBatchRequest.setDatas(agentDeductRequests);
//            agentDeductBatchRequest.setNum(agentDeductRequests.size());
            try {
                agentChannelService.deductBatch(agentDeductBatchRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void rcCallPhone() {
        List<BorrowList> borrowLists = borrowListMapper.selectUnBaikelu();
        if(borrowLists != null){
            for(BorrowList borrowList : borrowLists){
                try {
                    Map<String, Object> map = cardMapper.queryRobot(borrowList.getId());
                    boolean isWhite = riskWhiteService.isWhite(map.get("phone").toString());
                    //如果白名单用户,不拨打电话
                    if(!isWhite){
                        robotService.sendRcOrder(borrowList.getId());
                    }
                } catch (Exception e) {
                    logger.error("百可录打电话失败ID：" + borrowList.getId(), e);
                }
            }
        }
    }

    @Override
    public void batchQueryResult() {
        List<BorrowDeductions> list1 = loanOrderDOMapper.selectBankCardError();
        Date date = new Date();
        insertBorrowDeductions(list1, date);
        List<BorrowDeductions> list = loanOrderDOMapper.selectOverDueOrderStatus();
        insertBorrowDeductions(list, date);
    }

    private void insertBorrowDeductions(List<BorrowDeductions> list, Date date) {
        list.forEach(borrowDeductions -> {
            BorrowDeductions borrowDeductions1 = borrowDeductionsMapper.selectByBorrId(borrowDeductions.getBorrId());
            borrowDeductions.setUpdateDate(date);
            if(borrowDeductions1 == null){
                borrowDeductions.setCreateDate(date);
                borrowDeductionsMapper.insert(borrowDeductions);
            }else{
                borrowDeductions.setId(borrowDeductions1.getId());
                borrowDeductions.setCreateDate(borrowDeductions1.getCreateDate());
                borrowDeductionsMapper.updateByPrimaryKey(borrowDeductions);
            }
        });
    }

}
