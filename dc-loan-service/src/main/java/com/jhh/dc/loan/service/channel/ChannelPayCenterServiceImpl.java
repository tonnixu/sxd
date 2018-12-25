package com.jhh.dc.loan.service.channel;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jhh.dc.loan.api.channel.AgentBatchStateService;
import com.jhh.dc.loan.api.channel.AgentChannelService;
import com.jhh.dc.loan.api.channel.TradeBatchStateService;
import com.jhh.dc.loan.api.channel.TradePayService;
import com.jhh.dc.loan.api.constant.Constants;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.agreement.BatchCallback;
import com.jhh.dc.loan.api.entity.capital.*;
import com.jhh.dc.loan.api.white.RiskWhiteService;
import com.jhh.dc.loan.common.enums.AgentDeductResponseEnum;
import com.jhh.dc.loan.common.enums.AgentpayResultEnum;
import com.jhh.dc.loan.common.enums.PayTriggerStyleEnum;
import com.jhh.dc.loan.common.util.CodeReturn;
import com.jhh.dc.loan.common.util.HttpUrlPost;
import com.jhh.dc.loan.common.util.RedisConst;
import com.jhh.dc.loan.common.util.thread.AsyncExecutor;
import com.jhh.dc.loan.common.util.thread.runner.AbstractSimpleRunner;
import com.jhh.dc.loan.entity.OrderExt;
import com.jhh.dc.loan.entity.PersonInfoDto;
import com.jhh.dc.loan.entity.app.*;
import com.jhh.dc.loan.entity.callback.LKLBatchCallback;
import com.jhh.dc.loan.entity.enums.SpecialUserEnum;
import com.jhh.dc.loan.mapper.app.*;
import com.jhh.dc.loan.mapper.gen.LoanOrderDOMapper;
import com.jhh.dc.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.dc.loan.mapper.loan.PayChannelAdapterMapper;
import com.jhh.dc.loan.mapper.product.ProductCompanyExtMapper;
import com.jhh.dc.loan.mapper.product.ProductExtMapper;
import com.jhh.dc.loan.service.capital.BasePayServiceImpl;
import com.jhh.dc.loan.task.BatchDeductTimerTask;
import com.jhh.dc.loan.util.PostAsync;
import com.jhh.pay.driver.pojo.PayResponse;
import com.jhh.pay.driver.pojo.SimpleOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 2018/3/30.
 */
@Service
@Slf4j
public class ChannelPayCenterServiceImpl extends BasePayServiceImpl implements AgentChannelService, AgentBatchStateService {

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;

    @Autowired
    private JdCardInfoMapper jdCardInfoMapper;

    @Autowired
    private PayChannelAdapterMapper payChannelAdapterMapper;

    @Autowired
    private ProductCompanyExtMapper productCompanyExtMapper;
    @Resource(name = "tradePayService")
    private TradePayService tradeLocalService;

    @Value("${isTest}")
    private String isTest;

    @Value("${batchDeductSize}")
    private String batchDeductSize;

    @Value("${A.synchrodata.borrowUrl}")
    private String borrowUrl;

    @Autowired
    private TradeBatchStateService tradeBatchStateService;

    @Autowired
    private RiskWhiteService riskWhiteService;

    @Override
    public ResponseDo<?> pay(AgentPayRequest pay) {
        log.info("\n[代付开始] -->走支付中心渠道 pay" + pay);
        // 幂等性操作 防止重复放款
        if (CodeReturn.PRODUCT_TYPE_CODE_CARD.equals(pay.getProdType())) {
            return payCard(pay);
        }
        ResponseDo responseDo = formLock(pay.getBorrId());
        if (CodeReturn.success != responseDo.getCode()) {
            return responseDo;
        }
        try {
            if (checkAgentPayLog(pay.getBorrId(), pay.getUserId())) {
                // 获取合同信息并更改合同状态
                PersonInfoDto dto = getPersonInfo(pay.getBorrId());
                //判断用户是否存在多个中间状态的合同
                int count = borrowListMapper.selectDoing(pay.getUserId());
                if (count > 1) {
                    //将当前合同改为放款失败
                    dto.getBorrowList().setBorrStatus(CodeReturn.STATUS_COM_PAY_FAIL);
                    dto.getBorrowList().setUpdateDate(new Date());
                    dto.getBorrowList().setDescription("用户有多笔进行中的合同");
                    borrowListMapper.updateByPrimaryKeySelective(dto.getBorrowList());
                    AsyncExecutor.syncExecute(new PostAsync<>(dto.getBorrowList(), borrowUrl, borrowListMapper));
                    return ResponseDo.newFailedDo("该用户有未结清的合同");
                }
                //查询用户当前是否在白名单
                boolean white = riskWhiteService.isWhite(dto.getPerson().getPhone());
                if (dto.getBorrowList().getWhiteList() != null && !white) {
                    //申请合同在白名单 现在不在白名单 取消合同
                    dto.getBorrowList().setBorrStatus(CodeReturn.STATUS_CANCEL);
                    dto.getBorrowList().setUpdateDate(new Date());
                    borrowListMapper.updateByPrimaryKeySelective(dto.getBorrowList());
                    AsyncExecutor.syncExecute(new PostAsync<>(dto.getBorrowList(), borrowUrl, borrowListMapper));
                    return ResponseDo.newFailedDo("当前用户已不再白名单中，该合同已被取消");
                }
                // 合同状态改为放款中
                dto.getBorrowList().setBorrStatus(CodeReturn.STATUS_COM_PAYING);
                dto.getBorrowList().setUpdateDate(new Date());
                borrowListMapper.updateByPrimaryKeySelective(dto.getBorrowList());
                AsyncExecutor.syncExecute(new PostAsync<>(dto.getBorrowList(), borrowUrl, borrowListMapper));
                // 生成流水号
                LoanOrderDO loanOrder = savePayLoanOrder(dto, Constants.payOrderType.PAYCENTER_PAY_TYPE, pay.getTriggerStyle(), Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
                //生成手续费订单
                //手续费
                String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "2");
                saveFeeOrder(loanOrder, fee);
                Bank bank = bankMapper.selectPrimayCardByPerId(String.valueOf(pay.getUserId()));
                //发起代付
                //获取代付产品对应appid
                String appId = productCompanyExtMapper.selectValueByProductId(dto.getBorrowList().getProdId(), Constants.PayStyleConstants.DC_PAY_APPID);
                TradeVo vo = new TradeVo(pay.getUserId(), loanOrder.getSerialNo(),
                        pay.getPayChannel(), pay.getTriggerStyle(), bank.getBankNum(), loanOrder.getActAmount().floatValue(), Integer.parseInt(appId));
                return tradeLocalService.postPayment(vo);
            } else {
                log.info("\n[代付] 此order已经有一笔单子在处理中");
                return ResponseDo.newFailedDo(AgentpayResultEnum.HAD_PROCESSING.getDesc());
            }
        } catch (Exception e) {
            log.error("支付中心代付出现异常", e);
            return ResponseDo.newFailedDo("系统繁忙");
        } finally {
            jedisCluster.del(RedisConst.PAYCONT_KEY + pay.getBorrId());
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class, propagation = Propagation.REQUIRED)
    public ResponseDo<?> payCard(AgentPayRequest pay) {
        log.info("\n[代付开始] -->走支付中心渠道 pay" + pay);

        // 幂等性操作 防止重复放款
        ResponseDo responseDo = formLock(pay.getBorrId());
        if (CodeReturn.success != responseDo.getCode()) {
            return responseDo;
        }
        try {
            if (checkAgentPayLog(pay.getBorrId(), pay.getUserId())) {
                //获取未使用京东卡
                JdCardInfo info = jdCardInfoMapper.getUnusedCard();
                if (info == null) {
                    return ResponseDo.newFailedDo("卡片已全部发放，请及时补充");
                }
                // 获取合同信息并更改合同状态
                PersonInfoDto dto = getPersonInfo(pay.getBorrId());
                // 合同状态改为放款中
                dto.getBorrowList().setBorrStatus(CodeReturn.STATUS_COM_PAYING);
                dto.getBorrowList().setUpdateDate(new Date());
                borrowListMapper.updateByPrimaryKeySelective(dto.getBorrowList());
                AsyncExecutor.execute(new PostAsync<>(dto.getBorrowList(), borrowUrl, borrowListMapper));
                // 生成流水号
                LoanOrderDO loanOrder = savePayLoanOrder(dto, Constants.payOrderType.JD_PAY_CARD, pay.getTriggerStyle(), Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
                info.setPerId(dto.getPerson().getId());
                info.setBorrId(dto.getBorrowList().getId());
                info.setProductId(dto.getBorrowList().getProdId());
                info.setReviewUser(dto.getBorrowList().getCollectionUser());
                jdCardInfoMapper.updateByPrimaryKeySelective(info);
                paySuccessAfter(loanOrder.getSerialNo());
                return ResponseDo.newSuccessDo();
            } else {
                log.info("\n[代付] 此order已经有一笔单子在处理中");
                return ResponseDo.newFailedDo(AgentpayResultEnum.HAD_PROCESSING.getDesc());
            }
        } catch (Exception e) {
            return ResponseDo.newFailedDo(e.getMessage());
        } finally {
            jedisCluster.del(RedisConst.PAYCONT_KEY + pay.getBorrId());
        }
    }

    @Override
    public ResponseDo state(String serNO) throws Exception {
        log.info("-------------支付中心查询订单号 " + serNO);
        //加入redis锁 防止重复提交
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + serNO, "off", "NX", "EX", 3 * 60))) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
        }
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_REFUND_KEY + serNO, "off", "NX", "EX", 3 * 60))) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有退款在处理中，请稍后");
        }
        try {
            ResponseDo<?> state = tradeLocalService.state(serNO);
            if (state != null) {
                afterStateHandle(state, serNO);
                return state;
            } else {
                return ResponseDo.newFailedDo("查询失败，请稍候再试");
            }
        } catch (Exception e) {
            log.info("出错：" + e.getMessage(), e);
            return new ResponseDo<>(204, "处理中,请稍候");
        } finally {
            jedisCluster.del(RedisConst.PAY_ORDER_KEY + serNO);
            jedisCluster.del(RedisConst.PAY_REFUND_KEY + serNO);
        }
    }

    @Override
    public ResponseDo batchState(List<String> loanOrder) throws Exception {
        log.info("-------------支付中心查询订单号 loanOrder = " + loanOrder.toString());
        //将list切割
        List<List<String>> partition = Lists.partition(loanOrder, Integer.parseInt(batchDeductSize));
        if (partition.size() < 1) {
            return ResponseDo.newSuccessDo();
        }
        partition.forEach(v -> {
            //防止重复提交
            lock(v);
            if (v.size() < 1) {
                return;
            }
            try {
                ResponseDo<PayResponse> result = tradeBatchStateService.batchState(v);
                if (!(result != null && Constants.CommonPayResponseConstants.SUCCESS_CODE == result.getCode())) {
                    return;
                }
                PayResponse resp = result.getData();
                if (Constants.PayStyleConstants.JHH_PAY_STATE_PROGRESSING.equals(resp.getState())
                        || Constants.PayStyleConstants.JHH_PAY_STATE_SUCCESS.equals(resp.getState())) {
                    //更新sid等信息
                    updateOrderBySid(resp.getSimpleOrders(), resp.getSimpleOrders().get(0).getChannelKey());
                    //查询失败订单
                    Map<String, String> failMap = resp.getSimpleOrders().stream().filter(f -> Constants.PayStyleConstants.JHH_PAY_STATE_ERROR.equals(f.getState())
                            || Constants.PayStyleConstants.JHH_PAY_STATE_FAIL.equals(f.getState()))
                            .collect(Collectors.toMap(SimpleOrder::getOrderNo, SimpleOrder::getMsg));
                    if (failMap != null && failMap.size() > 0 ) {
                        loanOrderDOMapper.updateOrderByFail(failMap);
                    }
                    resp.getSimpleOrders().forEach(f -> {
                        if (Constants.PayStyleConstants.JHH_PAY_STATE_SUCCESS.equals(f.getState())) {
                            LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(f.getOrderNo());
                            handleSuccess(loanOrderDO);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("", e);
            } finally {
                unlock(v);
            }
        });
        return null;
    }

    /**
     * 清除锁
     *
     * @param serialNos
     */
    private void unlock(List<String> serialNos) {
        serialNos.forEach(v -> jedisCluster.del(RedisConst.PAY_ORDER_KEY + v));
    }

    /**
     * 上锁 如存在 则提剔除
     *
     * @param serialNos
     */
    private void lock(List<String> serialNos) {
        Iterator<String> iterator = serialNos.iterator();
        while (iterator.hasNext()) {
            if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + iterator.next(), "off", "NX", "EX", 3 * 60))) {
                iterator.remove();
            }
        }
    }

    @Override
    public void batchCallback(BatchCallback batchCallback) {
        //处理部分失败订单
        if (batchCallback != null && batchCallback.getExtension() != null && batchCallback.getExtension().get("exceptionMaps") != null) {
            Map<String, String> failOrder = (Map<String, String>) batchCallback.getExtension().get("exceptionMaps");
            log.info("批量代扣回调解析参数\n" + failOrder);
            try {
                int count = loanOrderDOMapper.updateOrderByFail(failOrder);
                log.info("批量代扣失败更新结束 更新条数 count = {}", count);
            } catch (Exception e) {
                log.error("批量失败更新抛出异常", e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public ResponseDo<?> deduct(AgentDeductRequest request) {
        log.info("[代扣开始] -->走支付中心渠道{} ", request);
        ResponseDo<String> result;
        //请结算锁
        result = settleLock(request.getTriggerStyle());
        if (AgentDeductResponseEnum.SUCCESS_CODE.getCode() != result.getCode()) {
            return result;
        }
        //加入redis锁 防止重复提交 TODO 并发时用其他方式返回值有问题
        Object jedis = jedisCluster.eval("return redis.call('set', KEYS[1],'1','nx', 'ex', '180') ", Collections.singletonList(RedisConst.PAY_ORDER_KEY + request.getBorrId()),new ArrayList<>());
        if (!"OK".equals(jedis)) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
        }
        try {
            ResponseDo<TradeVo> deductResult = doDeduct(request, "deduct");
            if (Constants.CommonPayResponseConstants.SUCCESS_CODE == deductResult.getCode()) {
                result = tradeLocalService.postDeduct(deductResult.getData());
                return result;
            } else {
                return deductResult;
            }
        } catch (Exception e) {
            log.error("出错：" + e.getMessage(), e);
            result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
            result.setInfo(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getMsg());
            return result;
        } finally {
            jedisCluster.del(RedisConst.PAY_ORDER_KEY + request.getBorrId());
        }
    }


    @Override
    public ResponseDo<?> deductBatch(AgentDeductBatchRequest requests) {
        log.info("[批量代扣开始] -->走支付中心渠道{} ", requests);
        if (requests == null) {
            return ResponseDo.newFailedDo("未获取需要批量的记录");
        }
        //清结算锁
        ResponseDo<?> result = settleLock(requests.getTriggerStyle());
        if (AgentDeductResponseEnum.SUCCESS_CODE.getCode() != result.getCode()) {
            return result;
        }
        //老公司主体订单
        List<TradeVo> oldDeduct = Collections.synchronizedList(new ArrayList<>());
        //新公司主体订单
        List<TradeVo> newDeduct = Collections.synchronizedList(new ArrayList<>());
        //将集合拆分
        requests.getRequests().parallelStream().forEach(request -> {
            //加入redis锁 防止重复提交
            Object jedis = jedisCluster.eval("return redis.call('set', KEYS[1],'1','nx', 'ex', '180') ", Collections.singletonList(RedisConst.PAY_ORDER_KEY + request.getBorrId()),new ArrayList<>());
            if (!"OK".equals(jedis)) {
                log.error("批量代扣订单中用正在处理的订单 borrNum = " + request.getBorrId());
                return;
            }
            try {
                ResponseDo<TradeVo> deductBatch = doDeduct(request, "deductBatch");
                if (Constants.CommonPayResponseConstants.SUCCESS_CODE == deductBatch.getCode()) {
                    //判断新老主体
                    if (Constants.PayStyleConstants.SXD_COMPANY_BODY_FALG.equals(deductBatch.getData().getCompanyBody())) {
                        oldDeduct.add(deductBatch.getData());
                    } else {
                        newDeduct.add(deductBatch.getData());
                    }
                }
            } catch (Exception e) {
                log.error("出错：" + e.getMessage(), e);
                result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
                result.setInfo(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getMsg());
            } finally {
                jedisCluster.del(RedisConst.PAY_ORDER_KEY + request.getBorrId());
            }
        });
        Queue<List<TradeVo>> queue = setQueue(oldDeduct, newDeduct);
        if (!queue.isEmpty()){
           new BatchDeductTimerTask().delayScheduleAtFixedRate(new BatchDeductTimerTask(queue, requests.getPayChannel(), requests.getOptPerson(), tradeLocalService), 0, 5, TimeUnit.SECONDS);
        }
        return ResponseDo.newSuccessDo("正在火速处理，请稍候查看");

    }

    @Override
    public void batchExpireDeduct() {
        List<BorrowList> expireDeduct = borrowListMapper.selectExpireBorrow();
        List<AgentDeductRequest> agentDeductRequests = new ArrayList<>();
        expireDeduct.forEach(v->{
            Person p = personMapper.selectByPrimaryKey(v.getPerId());
            Bank bank = bankMapper.selectPrimayCardByPerId(String.valueOf(p.getId()));
            AgentDeductRequest request = new AgentDeductRequest();
            request.setGuid(UUID.randomUUID().toString());
            request.setBorrNum(v.getBorrNum());
            request.setBorrId(String.valueOf(v.getId()));
            request.setDescription("到期日批量代扣");
            request.setIdCardNo(p.getCardNum());
            request.setName(p.getName());
            request.setPhone(p.getPhone());
            request.setCreateUser(SpecialUserEnum.USER_SYS.getCode());
            request.setTriggerStyle("0");
            request.setType("2"); //正常结清 ，只结清当期
            request.setBankId(String.valueOf(bank.getId()));
            request.setBankNum(bank.getBankNum());
            request.setBankName(bank.getBankName());
            request.setBankCode(bank.getBankCode());
            request.setOptAmount(String.valueOf(v.getAmountSurplus()));
            agentDeductRequests.add(request);
        });
        AgentDeductBatchRequest batchRequest = new AgentDeductBatchRequest();
        batchRequest.setDeductSize(agentDeductRequests.size());

        batchRequest.setOptPerson(SpecialUserEnum.USER_SYS.getCode());
        batchRequest.setTriggerStyle("0");
        batchRequest.setRequests(agentDeductRequests);
        deductBatch(batchRequest);
    }

    /**
     * @param request
     * @param deductType
     * @return
     */
    private ResponseDo<TradeVo> doDeduct(AgentDeductRequest request, String deductType) {

        BorrowList borrow = borrowListMapper.getBorrowListByBorrId(Integer.parseInt(request.getBorrId()));
        //更新代扣时间
        borrow.setCurrentRepayTime(new Date());
        borrowListMapper.updateByPrimaryKeySelective(borrow);
        //查询用户
        Person person = personMapper.selectByPrimaryKey(borrow.getPerId());
        //修改request参数
        ResponseDo<?> responseDo = updateAgentDeductRequest(request, borrow, person);
        if (!AgentDeductResponseEnum.SUCCESS_CODE.getCode().equals(responseDo.getCode())) {
            return ResponseDo.newFailedDo(responseDo.getInfo());
        }
        //提前结清，正常结算这俩中类型做金额判断
        NoteResult canPay = canPayCollect(borrow, Double.parseDouble(request.getOptAmount()), request.getType());
        if (!CodeReturn.SUCCESS_CODE.equals(canPay.getCode())) {
            return ResponseDo.newFailedDo(canPay.getInfo());
        }
        String type = "deduct".equals(deductType) ? setTypeAndSerialNo(request) : Constants.payOrderType.PAYCENTER_DEDUCTBATCH_TYPE;
        LoanOrderDO loanOrder = saveDeductLoanOrder(request, person.getId(), borrow.getId(), type, Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
        //从快速编码表查出手续费  1：代收
        String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "1");
        //修改 在第三方受理成功之前，生成手续费订单
        saveFeeOrder(loanOrder, fee);
        //发起代扣请求，请求统一支付中心
        Float finalAmount = (new BigDecimal(request.getOptAmount()).add(new BigDecimal(fee))).floatValue();
        //发起代扣
        String appId;
        if (PayTriggerStyleEnum.USER_TRIGGER.getCode().equals(Integer.parseInt(request.getTriggerStyle()))) {
            appId = productCompanyExtMapper.selectValueByProductId(borrow.getProdId(), Constants.PayStyleConstants.DC_REPAY_SWITCH);
        } else {
            appId = productCompanyExtMapper.selectValueByProductId(borrow.getProdId(),
                    "deduct".equals(deductType) ? Constants.PayStyleConstants.DC_DEDUCT_APPID : Constants.PayStyleConstants.DC_BATCH_APPID);
        }

        TradeVo vo = new TradeVo(person.getId(), loanOrder.getSerialNo(), request.getPayChannel(),
                Integer.parseInt(request.getTriggerStyle()), request.getBankNum(), finalAmount, Integer.parseInt(appId));
        vo.setValidateCode(request.getValidateCode());
        vo.setMsgChannel(request.getMsgChannel());
        vo.setPayType(request.getPayType());
        vo.setCompanyBody(borrow.getCompanyBody());
        vo.setName(person.getName());
        vo.setPhone(person.getPhone());
        vo.setBankName(request.getBankName());
        vo.setBankCode(request.getBankCode());
        vo.setCardNum(request.getIdCardNo());
        return ResponseDo.newSuccessDo(vo);
    }


    private String setTypeAndSerialNo(AgentDeductRequest request) {
        String type;
        if (PayTriggerStyleEnum.USER_TRIGGER.getCode().toString().equals(request.getTriggerStyle())) {
            type = Constants.payOrderType.PAYCENTER_DEDUCT_TYPE;
        } else {
            type = Constants.payOrderType.PAYCENTER_INITIATIVE_TYPE;
        }
        return type;
    }

    @Override
    public ResponseDo<?> refund(AgentRefundRequest refund) {
        //加入redis锁 防止重复提交
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_REFUND_KEY + refund.getPerId(), "off", "NX", "EX", 3 * 60))) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
        }

//        if(verifyLoanOrderStatus(refund.getPerId(),Constants.payOrderType.PAYCENTER_REFUND_PAY_TYPE)){
//            return ResponseDo.newFailedDo("存在未完成的退款，请先稍等");
//        }

        //获取银行卡id
        Bank bank = bankMapper.selectByBankNumEffective(refund.getBankNum(), refund.getPerId());
        if (bank == null || !bank.getPerId().equals(refund.getPerId())) {
            return ResponseDo.newFailedDo("该银行卡不存在，请验证");
        }

        //生成订单
        LoanOrderDO loanOrder = savePayLoanOrder(refund, bank.getId(), Constants.payOrderType.PAYCENTER_REFUND_PAY_TYPE, Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
        String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "5");
        saveFeeOrder(loanOrder, fee);
        //发起付款
        TradeVo tradeVo = new TradeVo(refund.getPerId(), loanOrder.getSerialNo(), refund.getPayChannel(),
                refund.getTriggerStyle(), bank.getBankNum(), refund.getAmount().floatValue(), null);
        ResponseDo<String> result = new ResponseDo<>();
        try {
            ResponseDo<String> responseDo = tradeLocalService.refund(tradeVo);
            log.info("支付中心退款返回结果 responseDo = {}", responseDo);
            result.setCode(responseDo.getCode());
            result.setInfo(responseDo.getInfo());
            result.setData(loanOrder.getSerialNo());
            return result;
        } catch (Exception e) {
            log.error("出错：" + e.getMessage(), e);
            result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
            result.setInfo(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getMsg());
            return result;
        } finally {
            jedisCluster.del(RedisConst.PAY_REFUND_KEY + refund.getPerId());
        }
    }


    private Queue<List<TradeVo>> setQueue(List<TradeVo> oldDeduct, List<TradeVo> newDeduct) {
        Queue<List<TradeVo>> queue = new LinkedBlockingDeque<>();
        List<List<TradeVo>> oldPartition = Lists.partition(oldDeduct, Integer.parseInt(batchDeductSize));
        List<List<TradeVo>> newPartition = Lists.partition(newDeduct, Integer.parseInt(batchDeductSize));
        oldPartition.forEach(queue::offer);
        newPartition.forEach(queue::offer);
        return queue;
    }

    private void updateOrderBySid(List<SimpleOrder> serialNo, String channelKey) {
        List<OrderExt> extlist = new ArrayList<>();
        serialNo.forEach(v -> {
            OrderExt ext = new OrderExt();
            ext.setSerialNo(v.getOrderNo());
            ext.setSid(v.getSid());
            extlist.add(ext);
        });
        String channel = payChannelAdapterMapper.getChannelBypayCenterAndType(channelKey, Constants.payOrderType.PAYCENTER_DEDUCTBATCH_TYPE);
        if (StringUtils.isEmpty(channel)) {
            channel = channelKey;
        }
        loanOrderDOMapper.updateOrderByChannel(extlist, channel);
    }

}
