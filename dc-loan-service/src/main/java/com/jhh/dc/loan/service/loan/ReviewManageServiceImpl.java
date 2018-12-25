package com.jhh.dc.loan.service.loan;

import com.alibaba.fastjson.JSONObject;
import com.jhh.dc.loan.api.channel.AgentChannelService;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.capital.AgentPayRequest;
import com.jhh.dc.loan.api.loan.ReviewManageService;
import com.jhh.dc.loan.common.util.Detect;
import com.jhh.dc.loan.constant.Constant;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.common.Constants;
import com.jhh.dc.loan.entity.common.ResponseCode;
import com.jhh.dc.loan.entity.enums.BorrowStatusEnum;
import com.jhh.dc.loan.entity.loan_vo.ResponseVo;
import com.jhh.dc.loan.entity.manager.Review;
import com.jhh.dc.loan.mapper.app.BorrowListMapper;
import com.jhh.dc.loan.mapper.app.PersonMapper;
import com.jhh.dc.loan.mapper.manager.ReviewMapper;
import com.jhh.dc.loan.mapper.product.ProductCompanyExtMapper;
import com.jhh.dc.loan.mapper.product.ProductExtMapper;
import com.jhh.pay.driver.pojo.BindChannelsRequest;
import com.jhh.pay.driver.pojo.QueryBindChannelResp;
import com.jhh.pay.driver.service.TradeService;
import com.jinhuhang.risk.client.dto.QueryResultDto;
import com.jinhuhang.risk.client.service.impl.blacklist.BlacklistAPIClient;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 审核管理dubbo服务实现类
 */
@Service
@Log4j
public class ReviewManageServiceImpl implements ReviewManageService {

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private PersonMapper personMapper;

    private BlacklistAPIClient riskClient = new BlacklistAPIClient();;

    @Autowired
    private AgentChannelService agentChannelService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ProductCompanyExtMapper productCompanyExtMapper;

    /**
     * 审核放款
     * @param borrId
     * @param userNum
     * @param payChannel
     * @return
     */
    @Override
    public ResponseVo pay(Integer borrId, String userNum, String payChannel) {
        ResponseVo response = new ResponseVo().code(ResponseCode.FIAL).msg("放款失败");
        if(borrId==null){
            response.setMsg("放款失败，合同号为空");
            return response;
        }
        if(StringUtils.isEmpty(userNum)){
            response.setMsg("放款失败，审核人为空");
            return response;
        }
        BorrowList bl = borrowListMapper.selectByPrimaryKey(borrId);
        if(bl==null){
            response.setMsg("放款失败，合同信息为空");
            return response;
        }

        // 判断该用户是否是黑名单用户，如果是黑名单用户，提示“该用户是黑名单用户”，把该用户的状态改成“电审未通过”
        try {
            Map<String,String> customerInfo = personMapper.getCardNumAndPhoneByBorrId(borrId);
            QueryResultDto queryResultDto = riskClient.blacklistSingleQuery(customerInfo.get("cardNum"),customerInfo.get("phone"));
            log.error("queryResultDto:" + JSONObject.toJSONString(queryResultDto));
            if(Constant.IS_BLACK.equals(String.valueOf(queryResultDto.getModel()))){
                response.setMsg("该用户为黑名单用户, 放款失败");
                borrowListMapper.updateStatusById(borrId, BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode());
                return response;
            }
        } catch (Exception e) {
            log.error("调用黑名单接口失败:" + ExceptionUtils.getFullStackTrace(e));
        }

        //合同状态为已缴费和失败的可以放款
        if (bl.getBorrStatus().equals(BorrowStatusEnum.PAY_SUCESS.getCode()) ||
                bl.getBorrStatus().equals(BorrowStatusEnum.LOAN_FAIL.getCode()) ||
                bl.getBorrStatus().equals(BorrowStatusEnum.WAIT_LOAN.getCode())) {
            AgentPayRequest request = new AgentPayRequest(bl.getPerId(), bl.getId() + "", 1, payChannel);
            request.setProdType(bl.getProdType());
            //判断用户是否绑卡
            ResponseVo bindBank = getValidBankList(bl.getPerId());
            if(bindBank != null && bindBank.getData() != null){
                boolean isBindBank = false;
                for(Object bank: (List) bindBank.getData()){
                    //主卡是否绑定过快捷支付
                    if(((Map)bank).get("status").equals("主卡") && Detect.notEmpty(((Map)bank).get("quickBinding") + "")){
                        isBindBank = true;
                    }
                }

                if(!isBindBank){
                    response.setCode(201);
                    response.setMsg("该用户未绑定任何银行卡，不能放款！");
                    return response;
                }
            }
            ResponseDo<?> result = agentChannelService.pay(request);
            if (result != null) {
                response.setCode(result.getCode());
                response.setMsg(result.getInfo());
            }
        } else {
            response.msg("系统异常，合同状态不符，请刷新页面！");
        }
        return response;
    }

    /**
     * 风控转件
     */
    @Override
    public ResponseVo transfer(String borrIds, String userNum) {
        ResponseVo response = new ResponseVo().code(ResponseCode.FIAL).msg("操作失败");
        if (Detect.notEmpty(borrIds) && Detect.notEmpty(borrIds)) {
            String[] ids = borrIds.split(",");
            List status = new ArrayList();
            status.add(BorrowStatusEnum.SIGNED.getCode());
            status.add(BorrowStatusEnum.PAY_SUCESS.getCode());
            status.add(BorrowStatusEnum.WAIT_LOAN.getCode());
            //只有已签约和已缴费能转件
            List<Integer> transferIds = borrowListMapper.selectIdsByStatus(Arrays.asList(ids), status);
            for (Integer id : transferIds) {
                saveReview(id, "", userNum);
            }
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        }
        return response;
    }

    //查询支付中心用户绑卡信息
    private ResponseVo getValidBankList(Integer personId) {
        List banks = null;
        if (Detect.isPositive(personId)) {
            banks = personMapper.getBankByPerId(personId);
            banks.removeIf(e -> ((Map) e).get("status").equals("无效卡"));
            try {
                List<String> list = new ArrayList<>();
                banks.stream().forEach(t -> list.add(((Map) t).get("bankNum").toString()));
                log.info("开始查询绑卡信息,银行卡号：" + list.toString());
                BindChannelsRequest b = new BindChannelsRequest();
                BorrowList nowBorr = borrowListMapper.selectNow(personId);
                String appId = productCompanyExtMapper.selectValueByProductId(nowBorr.getProdId(),com.jhh.dc.loan.api.constant.Constants.PayStyleConstants.DC_BIND_CARD_APPID);
                b.setAppId(Integer.parseInt(appId));
                b.setBankCards(list);
                QueryBindChannelResp queryBindChannelResp = tradeService.queryBindChannel(b);

                if(queryBindChannelResp != null && "200".equals(queryBindChannelResp.getCode())){
                    for (int i = 0; i < banks.size(); i++) {
                        Map<String, Object> bank = (Map<String, Object>) banks.get(i);
                        String[] bindingList = ((JSONObject) queryBindChannelResp.getData()).getJSONArray((String) bank.get("bankNum")).toArray(new String[0]);
                        bank.put("quickBinding", String.join("，", bindingList));
                    }
                }
            }catch(Exception e){
                log.info("查询绑卡渠道异常",e);
            }
        }

        return new ResponseVo().code(200).data(banks);
    }

    public ResponseVo  saveReview(Integer borrId, String reason, String employNum) {
        ResponseVo response = new ResponseVo().code(ResponseCode.FIAL).msg("操作失败");
        if(borrId==null){
            response.setMsg("合同Id不能为空");
            return response;
        }
        if(StringUtils.isEmpty(employNum)){
            response.setMsg("操作人不能为空");
            return response;
        }
        Review review = new Review();
        review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
        review.setBorrId(borrId);

        List<Review>  reviews= reviewMapper.selectReviewByBorrIdAndReviewType(review);

        if (reviews != null&&reviews.size()>0) {
            review=reviews.get(0);
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
            review.setBorrId(borrId);
            review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
            review.setReason(reason);
            review.setEmployNum(employNum);
            review.setCreateDate(Calendar.getInstance().getTime());
            reviewMapper.insertSelective(review);
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        }
        return response;
    }
}
