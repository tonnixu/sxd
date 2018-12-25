package com.jhh.dc.loan.manage.service.impl;

import com.alibaba.fastjson.JSON;
import com.jhh.dc.loan.api.channel.AgentChannelService;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.capital.AgentPayRequest;
import com.jhh.dc.loan.api.loan.ReviewManageService;
import com.jhh.dc.loan.common.util.HttpUtils;
import com.jhh.dc.loan.common.util.thread.AsyncExecutor;
import com.jhh.dc.loan.entity.app.BankVo;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.common.Constants;
import com.jhh.dc.loan.entity.common.ResponseCode;
import com.jhh.dc.loan.entity.enums.BorrowStatusEnum;
import com.jhh.dc.loan.entity.loan_vo.ResponseVo;
import com.jhh.dc.loan.entity.manager.Review;
import com.jhh.dc.loan.manage.entity.Response;
import com.jhh.dc.loan.manage.mapper.BorrowListMapper;
import com.jhh.dc.loan.manage.mapper.PersonMapper;
import com.jhh.dc.loan.manage.mapper.ReviewMapper;
import com.jhh.dc.loan.manage.service.risk.ReviewService;
import com.jhh.dc.loan.manage.service.user.UserService;
import com.jhh.dc.loan.manage.utils.Assertion;
import com.jhh.dc.loan.manage.utils.Detect;
import com.jhh.dc.loan.manage.utils.PostAsync;
import com.jhh.dc.loan.manage.utils.PostListAsync;
import com.jhh.pay.driver.pojo.BankInfo;
import com.jinhuhang.risk.client.dto.QueryResultDto;
import com.jinhuhang.risk.client.service.impl.blacklist.BlacklistAPIClient;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
@Log4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private AgentChannelService agentChannelService;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private BlacklistAPIClient riskClient;

    @Autowired
    private ReviewManageService reviewManageService;

    @Value("${A.synchrodata.borrowUrl}")
    private String aSynchrodataBorrowUrl;

    @Value("${A.synchrodata.borrowListUrl}")
    private String aSynchrodataBorrowListUrl;

    public String getaSynchrodataBorrowListUrl() {
        return aSynchrodataBorrowListUrl;
    }

    public void setaSynchrodataBorrowListUrl(String aSynchrodataBorrowListUrl) {
        this.aSynchrodataBorrowListUrl = aSynchrodataBorrowListUrl;
    }

    @Value("${refund.service.fee}")
    private String refundServiceFeeUrl;

    /**
     * 操作审核结果
     * @param needState    符合的状态
     * @param saveStatus   修改的状态
     * @param bl            需要修改合同
     * @param reason       修改原因
     * @param userNum      修改人
     * @param errorrMessage      异常信息
     * @return
     */
    private Response operationReviewResult(List<String> needState, String saveStatus, BorrowList bl, String reason, String userNum, String errorrMessage){
        if(bl != null && saveStatus != null && needState != null){
            //是否满足合同修改状态
            if(needState.contains(bl.getBorrStatus())){
                //更新合同状态
                bl.setBorrStatus(saveStatus);
                borrowListMapper.updateByPrimaryKeySelective(bl);
                //更新审核结果
                return saveReview(bl.getId(), reason, userNum);
            }
        }
        return new Response().code(ResponseCode.FIAL).msg(errorrMessage);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = java.lang.Exception.class)
    public Response saveManuallyReview(Integer borroId, String reason, String userNum, Integer operationType) throws Exception {
        Assertion.isPositive(borroId, "合同Id不能为空");
        Assertion.isPositive(operationType, "操作类型不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");

        BorrowList bl = borrowListMapper.selectByPrimaryKey(borroId);
        if (bl != null) {
            List needStatus = new ArrayList();
            if (Constants.OperationType.MANUALLY_PASS.equals(operationType)) {

                needStatus.add(BorrowStatusEnum.SIGNED.getCode());
                response = operationReviewResult(needStatus, BorrowStatusEnum.WAIT_PAY.getCode(),
                        bl, reason, userNum,"已签约状态的合同才能审核通过");

                //同步A平台状态
                AsyncExecutor.execute(new PostAsync<>(bl, aSynchrodataBorrowUrl));

            } else if (Constants.OperationType.MANUALLY_REJECT.equals(operationType)) {

                needStatus.add(BorrowStatusEnum.SIGNED.getCode());
                needStatus.add(BorrowStatusEnum.WAIT_LOAN.getCode());
                response = operationReviewResult(needStatus, BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode(),
                        bl, reason, userNum,"已签约和待还款状态的合同才能拒绝");

                //同步A平台状态
                AsyncExecutor.execute(new PostAsync<>(bl, aSynchrodataBorrowUrl));
            }else if (Constants.OperationType.BLACK_REJECT.equals(operationType)) {

                needStatus.add(BorrowStatusEnum.SIGNED.getCode());
                needStatus.add(BorrowStatusEnum.WAIT_LOAN.getCode());

                response = operationReviewResult(needStatus, BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode(),
                        bl, reason, userNum,"已签约和待还款状态的合同才能拒绝并拉黑");

                //拉黑接口
                response = userService.userBlockWhite(bl.getPerId(), userNum, "",
                        reason, Constants.UserBlockWhite.BLACK);
                //同步A平台状态
                AsyncExecutor.execute(new PostAsync<>(bl, aSynchrodataBorrowUrl));
            } else if (Constants.OperationType.WHITE.equals(operationType)) {
                //洗白接口
                response = userService.userBlockWhite(bl.getPerId(), userNum, "",
                        reason, Constants.UserBlockWhite.WHITE);
            }else if (Constants.OperationType.WHITE.equals(operationType)) {
                //拉黑接口
                response = userService.userBlockWhite(bl.getPerId(), userNum, "",
                        reason, Constants.UserBlockWhite.BLACK);

            }
        }
        return response;
    }

    @Override
    public Response saveReview(Integer borroId, String reason, String employNum) {
        Assertion.isPositive(borroId, "合同Id不能为空");
        Assertion.notEmpty(employNum, "操作人不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");

        Review review = new Review();
        review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
        review.setBorrId(borroId);
        review = reviewMapper.selectOne(review);

        if (review != null) {
            if (review.getEmployNum().equals(employNum)) {
                //如果为同一个人操作直接更新
                review.setReason(reason);
                reviewMapper.updateByPrimaryKeySelective(review);
            } else {
                //不同人，更新历史,在插入审核记录
                review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW_HISTORY);
                reviewMapper.updateByPrimaryKeySelective(review);

                review.setId(null);
                review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
                review.setReason(reason);
                review.setEmployNum(employNum);
                review.setCreateDate(Calendar.getInstance().getTime());
                reviewMapper.insertSelective(review);
            }
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        } else {
            //没分过单的合同直接入库
            review = new Review();
            review.setBorrId(borroId);
            review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
            review.setReason(reason);
            review.setEmployNum(employNum);
            review.setCreateDate(Calendar.getInstance().getTime());
            reviewMapper.insertSelective(review);
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        }
        return response;
    }

    @Override
    public Response cancel(String brroIds, String reason, String userNum) {
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");

        if (Detect.notEmpty(brroIds) && Detect.notEmpty(brroIds)) {
            String[] ids = brroIds.split(",");
            //查询出符合状态记录
            List status = new ArrayList();
            status.add(BorrowStatusEnum.SIGNED.getCode());
            status.add(BorrowStatusEnum.WAIT_PAY.getCode());
            status.add(BorrowStatusEnum.PAY_SUCESS.getCode());
            status.add(BorrowStatusEnum.WAIT_LOAN.getCode());
            status.add(BorrowStatusEnum.LOAN_FAIL.getCode());
            List cancelIds = borrowListMapper.selectIdsByStatus(Arrays.asList(ids), status);
            if(Detect.notEmpty(cancelIds)){
                //查询出已缴费记录
                status = new ArrayList();
                status.add(BorrowStatusEnum.PAY_SUCESS.getCode());
                List pushborrNums = borrowListMapper.selectBorrNumByStatus(Arrays.asList(ids), status);

                //更新成已取消
                Example example = new Example(BorrowList.class);
                example.createCriteria().andIn("id", cancelIds);
                BorrowList bl = new BorrowList();
                bl.setBorrStatus(BorrowStatusEnum.CANCEL.getCode());
                borrowListMapper.updateByExampleSelective(bl, example);

                //更新催收人
                Example reviewExample = new Example(Review.class);
                reviewExample.createCriteria().andIn("borrId",cancelIds);
                Review review = new Review();
                review.setEmployNum(userNum);
                review.setReason(reason);
                reviewMapper.updateByExampleSelective(review, reviewExample);

                // 通知A平台退还咨询费
                if(Detect.notEmpty(pushborrNums)){
                    refundServiceFee(pushborrNums, reason, userNum);
                }


                //同步A平台状态
                AsyncExecutor.execute(new PostListAsync<>(borrowListMapper.selectByExample(example), aSynchrodataBorrowListUrl));

                response.code(ResponseCode.SUCCESS).msg("操作成功");
            }else {
                response.code(ResponseCode.SUCCESS).msg("该状态不允许取消");
            }

        }
        return response;
    }

    public void refundServiceFee(List pushIds, String reason, String userNum){
        try {
            if(Detect.notEmpty(pushIds)){
                String borrNum=String.join(",", pushIds);
                Map map = new HashMap();
                map.put("borrNum", borrNum);
                map.put("remark", reason);
                map.put("operator", userNum);
                HttpUtils.sendPost(refundServiceFeeUrl,HttpUtils.toParam(map));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResponseVo transfer(String brroIds, String userNum) {
        return  reviewManageService.transfer(brroIds,userNum);
//        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
//
//        if (Detect.notEmpty(brroIds) && Detect.notEmpty(brroIds)) {
//            String[] ids = brroIds.split(",");
//            List status = new ArrayList();
//            status.add(BorrowStatusEnum.SIGNED.getCode());
//            status.add(BorrowStatusEnum.PAY_SUCESS.getCode());
//            //只有已签约和已缴费能转件
//            List<Integer> transferIds = borrowListMapper.selectIdsByStatus(Arrays.asList(ids), status);
//            for (Integer id : transferIds) {
//                saveReview(id, "", userNum);
//            }
//            response.code(ResponseCode.SUCCESS).msg("操作成功");
//        }
//        return response;
    }

    @Override
    public ResponseVo pay(Integer borrId, String userNum, String payChannel) {
        return reviewManageService.pay(borrId,userNum,payChannel);
//        Assertion.isPositive(borrId, "合同号不能为空");
//        Assertion.notEmpty(userNum, "审核人不能为空");
//        Response response = new Response().code(ResponseCode.FIAL).msg("放款失败");
//
//        BorrowList bl = borrowListMapper.selectByPrimaryKey(borrId);
//        Assertion.notNull(bl, "合同不存在");
//
//        // 校验用户认证节点状态
////        NoteResult note = riskService.checkBpm(Integer.toString(bl.getPerId()));
////        if (!CodeReturn.BPM_FINISH_CODE.equals(note.getCode())) {
////            response.setMsg("用户节点状态不正确【"+ note.getInfo() +"】");
////            return response;
////        }
//
//        // 判断该用户是否是黑名单用户，如果是黑名单用户，提示“该用户是黑名单用户”，把该用户的状态改成“电审未通过”
//        try {
//            Map<String,String> customerInfo = personMapper.getCardNumAndPhoneByBorrId(borrId);
//            QueryResultDto queryResultDto = riskClient.blacklistSingleQuery(customerInfo.get("cardNum"),customerInfo.get("phone"));
//            if("0".equals(queryResultDto.getCode())){
//                response.setMsg("该用户为黑名单用户, 放款失败");
//                borrowListMapper.updateStatusById(borrId, BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode());
//                return response;
//            }
//        } catch (Exception e) {
//            log.error("调用黑名单接口失败:" + ExceptionUtils.getFullStackTrace(e));
//        }
//
//        //合同状态为已缴费和失败的可以放款
//        if (bl.getBorrStatus().equals(BorrowStatusEnum.PAY_SUCESS.getCode()) ||
//                bl.getBorrStatus().equals(BorrowStatusEnum.LOAN_FAIL.getCode()) ||
//                bl.getBorrStatus().equals(BorrowStatusEnum.WAIT_LOAN.getCode())) {
//            AgentPayRequest request = new AgentPayRequest(bl.getPerId(), bl.getId() + "", 1, payChannel);
//            request.setProdType(bl.getProdType());
//            //判断用户是否绑卡
//            Response bindBank = userService.getValidBankList(bl.getPerId());
//            if(bindBank != null && bindBank.getData() != null){
//                boolean isBindBank = false;
//                for(Object bank: (List) bindBank.getData()){
//                    //主卡是否绑定过快捷支付
//                    if(((Map)bank).get("status").equals("主卡") && Detect.notEmpty(((Map)bank).get("quickBinding") + "")){
//                        isBindBank = true;
//                    }
//                }
//
//                if(!isBindBank){
//                    response.setCode(201);
//                    response.setMsg("该用户未绑定任何银行卡，不能放款！");
//                    return response;
//                }
//            }
//            ResponseDo<?> result = agentChannelService.pay(request);
//            if (result != null) {
//                response.setCode(result.getCode());
//                response.setMsg(result.getInfo());
//            }
//        } else {
//            response.msg("系统异常，合同状态不符，请刷新页面！");
//        }
//        return response;
    }

    private BankInfo assemblingParam(BankVo bankVo) {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankCard(bankVo.getBankNum());
        return bankInfo;
    }

    public String getaSynchrodataBorrowUrl() {
        return aSynchrodataBorrowUrl;
    }

    public void setaSynchrodataBorrowUrl(String aSynchrodataBorrowUrl) {
        this.aSynchrodataBorrowUrl = aSynchrodataBorrowUrl;
    }

    public String getRefundServiceFeeUrl() {
        return refundServiceFeeUrl;
    }

    public void setRefundServiceFeeUrl(String refundServiceFeeUrl) {
        this.refundServiceFeeUrl = refundServiceFeeUrl;
    }
}