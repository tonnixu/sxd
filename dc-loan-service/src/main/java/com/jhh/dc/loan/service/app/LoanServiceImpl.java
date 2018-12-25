package com.jhh.dc.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.dc.loan.api.app.LoanService;
import com.jhh.dc.loan.api.constant.LoanConstant;
import com.jhh.dc.loan.api.constant.StateCode;
import com.jhh.dc.loan.api.entity.*;
import com.jhh.dc.loan.api.enums.NodeEnum;
import com.jhh.dc.loan.api.loan.ReviewManageService;
import com.jhh.dc.loan.api.white.RiskWhiteService;
import com.jhh.dc.loan.common.util.*;
import com.jhh.dc.loan.common.util.thread.AsyncExecutor;
import com.jhh.dc.loan.constant.Constant;
import com.jhh.dc.loan.entity.app.Bank;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.app.Person;
import com.jhh.dc.loan.entity.app.Product;
import com.jhh.dc.loan.entity.app_vo.SignInfo;
import com.jhh.dc.loan.entity.enums.SpecialUserEnum;
import com.jhh.dc.loan.entity.manager.Review;
import com.jhh.dc.loan.mapper.app.*;
import com.jhh.dc.loan.mapper.contract.ContractMapper;
import com.jhh.dc.loan.mapper.manager.ReviewMapper;
import com.jhh.dc.loan.mapper.product.ProductMapper;
import com.jhh.dc.loan.util.PostAsync;
import com.jinhuhang.settlement.dto.SettlementResult;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCluster;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.jhh.dc.loan.common.util.CodeReturn.REPEAT_BIND;
import static com.jhh.dc.loan.common.util.CodeReturn.STATUS_TO_REPAY;


/**
 * 借款模块接口实现类
 *
 * @author xuepengfei
 *         2016年10月9日下午3:40:59
 */
@Service
public class LoanServiceImpl implements LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    //借款状态常量
    private static final String STATUS_APLLY = CodeReturn.STATUS_APLLY;//申请中
    private static final String STATUS_CANCEL = CodeReturn.STATUS_CANCEL;//已取消
    private static final String STATUS_WAIT_SIGN = CodeReturn.STATUS_WAIT_SIGN;//待签约
    private static final String STATUS_SIGNED = CodeReturn.STATUS_SIGNED;//已签约

    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ReviewersMapper reviewersMapper;
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private CodeValueMapper codeValueMapper;
    @Autowired
    private AgreementServiceImpl agreementService;
    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RiskWhiteService riskWhiteService;

    @Autowired
    private ReviewManageService reviewManageService;

    @Autowired
    private VerifyServiceImpl verifyService;

    @Value("${A.synchrodata.borrowUrl}")
    private String borrowUrl;

    @Value("${A.synchrodata.personUrl}")
    private String personUrl;

    @Value("${A.synchrodata.contract.personUrl}")
    private String contractUrl;

    @Override
    public String getCollectionUser(int borrId) {
        BorrowList borrow = borrowListMapper.getBorrowListByBorrId(borrId);
        if (borrow != null) {
            return borrow.getCollectionUser();
        }
        return null;
    }


    /**
     * 生成借款记录
     *
     * @return
     */
    private ResponseDo<BorrowList> borrowProduct(String userId,Product product, String reviewer) {
        logger.info("生成借款记录 userId =" + userId + "\nproduct=" + product);
        ResponseDo<BorrowList> result = new ResponseDo<>();
        // 幂等操作
        if (StringUtils.isEmpty(jedisCluster.get(RedisConst.BP_KEY + userId))) {
            String setnx = jedisCluster.set(RedisConst.BP_KEY + userId, userId, "NX", "EX", 60);
            if (!"OK".equals(setnx)) {
                result.setCode(StateCode.ORDER_REPEAT_CODE);
                result.setInfo(StateCode.ORDER_REPEAT_MSG);
                logger.error("borrowProduct直接返回，重复数据per_id" + userId);
                return result;
            }
        } else {
            result.setCode(StateCode.ORDER_REPEAT_CODE);
            result.setInfo(StateCode.ORDER_REPEAT_MSG);
            logger.error("borrowProduct直接返回，重复数据per_id" + userId);
            return result;
        }
        Person person = personMapper.selectByPrimaryKey(Integer.parseInt(userId));
        //验证用户申请次数
        if (!validApplyLimit(person.getPhone())){
            return ResponseDo.newFailedDo("今日申请次数已到达上限,请明日再试");
        }
        if (product == null || "D".equals(product.getStatus())){
            return ResponseDo.newFailedDo("暂不支持该产品，请返回选择其他产品");
        }
        //新建borr时先检查是否有申请中的借款
        Integer haveBorrowing = borrowListMapper.selectDoing(Integer.parseInt(userId));
        if (haveBorrowing > 0) {//有电审未通过，已结清，已取消，已逾期结清之外的借款
            logger.error("有电审未通过，已结清，已取消，已逾期结清之外的借款");
            result.setCode(StateCode.ORDER_UNFINISHED_CODE);
            result.setInfo(StateCode.ORDER_UNFINISHED_MSG);
            return result;
        }
        //查看用户是否是白名单
        boolean white = riskWhiteService.isWhite(person.getPhone());
        BorrowList newBlist = borrowListMapper.selectNow(Integer.parseInt(userId));
        BorrowList bl = new BorrowList(product, person, newBlist == null ? null : newBlist.getBorrStatus(), white);
        //保存借款信息
        borrowListMapper.insertSelective(bl);
        //创建审核人
        setReviewer(bl.getId(), reviewer);
        result.setCode(StateCode.SUCCESS_CODE);
        result.setInfo(StateCode.SUCCESS_MSG);
        result.setData(bl);
        //新建借款记录成功
        return result;
    }


    /**
     * 合同签约，状态改为已签约，添加签约时间
     *
     * @param borrId 合同id
     * @return
     */
    @Override
    //注解掉事物,确保在签约方法执行过程中,将合同状态改为BS003或者BS019
    //@Transactional(rollbackFor = Exception.class)
    public ResponseDo<?> signingBorrow(String borrId, String loanUse,Integer serviceFeePosition) {
        //构建结果对象，默认201 失败
        //borr_id查询到用户当前的借款表
        BorrowList borrowList = borrowListMapper.selectByPrimaryKey(Integer.valueOf(borrId));

        //把当前借款表的借款状态改为已签约,添加签约时间
        if (!STATUS_WAIT_SIGN.equals(borrowList.getBorrStatus())) {
            //如果借款表的状态不为待签约，不能签约
            return ResponseDo.newFailedDo("合同状态错误，无法签约");
        }
        if (Constant.SERVICE_FEE_POSITION_PLATFORM == serviceFeePosition){
            borrowList.setBorrStatus(STATUS_SIGNED);
        }else {
            borrowList.setBorrStatus(CodeReturn.STATUS_WAIT_LOAN);
        }
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        borrowList.setMakeborrDate(date);
        borrowList.setUpdateDate(date);
        borrowList.setLoanUse(loanUse);
        int i = borrowListMapper.updateByPrimaryKeySelective(borrowList);
        if (i > 0) {
            //同步告知A
            AsyncExecutor.syncExecute(new PostAsync<>(borrowList, borrowUrl, borrowListMapper));
            //更改成功
            // TODO 暂时关闭机器人审核，待百可录机器人打开后再开启此代码
            //signRobot(borrowList);
        }

        Integer perId = borrowList.getPerId();
        Person person = personMapper.selectByPrimaryKey(perId);
        // 判断是否是快牛白名单
        if(riskWhiteService.isWhite(person.getPhone())){
            // 判断快牛自动放款是否打开
            String autoPay = codeValueMapper.selectCodeByType(LoanConstant.WHITE_AUTO_SWITCH_CODE);
            if(LoanConstant.STATUS_OPEN.equals(autoPay) && CodeReturn.STATUS_WAIT_LOAN.equals(borrowList.getBorrStatus())){
                // 调用转件接口
                reviewManageService.transfer(borrId,SpecialUserEnum.USER_WHITE.getCode());
                // 调用放款接口
                reviewManageService.pay(Integer.parseInt(borrId),SpecialUserEnum.USER_WHITE.getCode(),null);
            }
        }else{
            Long count = borrowListMapper.getCompletedHistoryCount(perId);
            // 判断是续贷
            if(count > 0){
                // 判断百可录自动放款开关是否打开
                String noWhiteAutoPay = codeValueMapper.selectCodeByType(LoanConstant.BAIKELU_AUTO_SWITCH_CODE);
                if(LoanConstant.STATUS_OPEN.equals(noWhiteAutoPay) && CodeReturn.STATUS_WAIT_LOAN.equals(borrowList.getBorrStatus())){
                    // 调用转件接口
                    reviewManageService.transfer(borrId,SpecialUserEnum.USER_AUTO.getCode());
                    // 调用放款接口
                    reviewManageService.pay(Integer.parseInt(borrId),SpecialUserEnum.USER_AUTO.getCode(),null);
                }
            }else{
                // 调用百可录打电话 通过->自动放款, 不通过 -> 人工放款
                signRobot(borrowList);
            }
        }
        return ResponseDo.newSuccessDo();
    }

    /**
     * 取消借款申请。判断合同状态，在申请中的合同才能取消借款申请。
     *
     * @param per_id  用户ID
     * @param borr_id 合同id
     * @return
     */
    @Override
    public ResponseDo<?> cancelAskBorrow(String per_id, String borr_id) {
        //构建结果对象，默认201 失败
        try {
            //根据borr_id获取借款表
            BorrowList borrowList = borrowListMapper.selectByPrimaryKey(Integer.valueOf(borr_id));
            String status = borrowList.getBorrStatus();
            if (STATUS_APLLY.equals(status) || STATUS_WAIT_SIGN.equals(status)) {
                borrowList.setBorrStatus(STATUS_CANCEL);
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                borrowList.setUpdateDate(date);
                borrowListMapper.updateByPrimaryKeySelective(borrowList);
                //同步告知A
                AsyncExecutor.syncExecute(new PostAsync<>(borrowList, borrowUrl, borrowListMapper));
                //取消借款申请成功
                return ResponseDo.newSuccessDo();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDo.newFailedDo("服务器正在开小差，请稍候");
        }
        return null;
    }


    @Override
    public SignInfo getSignInfo(String phone) {
        //获取用户信息
        Person person = personMapper.getPersonByPhone(phone);
        //获取用户当前合同
        BorrowList borrowList = borrowListMapper.selectNow(person.getId());
        Product product = productMapper.selectByPrimaryKey(borrowList.getProdId());
        String signHint = codeValueMapper.selectCodeByType("sign_hint");
        //计算违约金利率
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);
        BigDecimal interest = new BigDecimal(product.getPenalty()).divide(new BigDecimal(product.getAmount()),4, RoundingMode.CEILING);
        signHint = signHint.replace("{interest}",percent.format(interest.doubleValue()))
                .replace("{forfeit_rate}",percent.format(product.getForfeitRate()));
        SignInfo signInfo = new SignInfo();
        signInfo.setPerId(person.getId());
        signInfo.setBorrId(borrowList.getId());
        signInfo.setName(person.getName());
        signInfo.setBorrAmount(borrowList.getBorrAmount());
        signInfo.setTermNum(borrowList.getTermNum());
        signInfo.setBankNum(person.getBankCard());
        signInfo.setPayAmount(product.getPayAmount());
        signInfo.setServiceFee(product.getServiceFee());
        signInfo.setServiceFeePosition(product.getServiceFeePosition());
        signInfo.setSignHint(signHint);
        //调用风控试算接口
        ResponseDo<SettlementResult> planInfo = agreementService.getPlanInfo(borrowList.getId());
        if (200 == planInfo.getCode()) {
            JSONObject obj = JSONObject.parseObject(planInfo.getData().getModel());
            signInfo.setInterestRate(obj.getBigDecimal("interest_sum"));
            signInfo.setPlanRepay(obj.getFloat("plan_repay"));
        } else {
            signInfo.setPlanRepay((float) 0);
            signInfo.setInterestRate(BigDecimal.ZERO);
        }
        signInfo.setTermDay(borrowList.getTermday());
        return signInfo;
    }


    @Override
    public ResponseDo<UserNodeDo> applyBorrow(String phone, String productId, String borrNum, String reviewer) {
        logger.info("用户申请借款接口调用 phone=" + phone + "borrNum=" + borrNum + "reviewer=" + reviewer);
        //用户首次登陆 向A获取数据
        Person person;
        String md5 = ""; //TODO:Md5验证最后写
        String param = "phone=" + phone + "&md5=" + md5;
        String result = HttpUrlPost.sendGet(personUrl, param);
        if (StringUtils.isEmpty(result)) {
            return ResponseDo.newFailedDo("用户获取失败，请重新申请");
        } else {
            person = JSONObject.parseObject(result, Person.class);
            if (person == null ){
                return ResponseDo.newFailedDo("用户获取失败，请重新申请");
            }
        }
        Person p = personMapper.getPersonByPhone(phone);
        if (p == null) {
            personMapper.insertSelective(person);
        }else {
            p.setIsManual(person.getIsManual());
            personMapper.updateByPrimaryKeySelective(p);
            person = p;
        }
        //根据产品id获取产品类型
        Product product = productMapper.selectByPrimaryKey(Integer.parseInt(productId));
        //获取当前用户借款信息
        BorrowList borrowList = borrowListMapper.selectNow(person.getId());
        try {
            //判断用户当前状态是否结清，结清则生成合同
            if (borrowList == null || CodeReturn.STATUS_PAY_BACK.equals(borrowList.getBorrStatus())
                    || CodeReturn.STATUS_CANCEL.equals(borrowList.getBorrStatus())
                    || CodeReturn.STATUS_REVIEW_FAIL.equals(borrowList.getBorrStatus())
                    || CodeReturn.STATUS_PHONE_REVIEW_FAIL.equals(borrowList.getBorrStatus())
                    || CodeReturn.STATUS_DELAY_PAYBACK.equals(borrowList.getBorrStatus())
                    || CodeReturn.STATUS_EARLY_PAYBACK.equals(borrowList.getBorrStatus())) {
                //生成合同
                ResponseDo<BorrowList> aDo = borrowProduct(String.valueOf(person.getId()), product, reviewer);
                if (200 == aDo.getCode()) {
                    borrowList = aDo.getData();
                } else {
                    //合同创建失败 抛出错误
                    return ResponseDo.newFailedDo(aDo.getInfo());
                }
            }
            return checkNode(person, product, borrowList);
        } catch (Exception e) {
            logger.error("e",e);
            return ResponseDo.newFailedDo("服务器繁忙，清稍候");
        } finally {
            //合同生成成功回调告知A公司
            BorrowList nowBorr = borrowListMapper.selectNow(person.getId());
            AsyncExecutor.syncExecute(new PostAsync<>(nowBorr, borrowUrl, borrowListMapper));
        }
    }

    private ResponseDo<UserNodeDo> checkNode(Person person, Product product, BorrowList borrowList) {
        //判断合同与产品关系
        if (!product.getProductTypeCode().equals(borrowList.getProdType())){
            return ResponseDo.newFailedDo("您当前有未完成的合同，无法再次申请");
        }
        UserNodeDo nodeDo = new UserNodeDo();
        nodeDo.setBorrNum(borrowList.getBorrNum());
        nodeDo.setProdType(product.getProductTypeCode());
        nodeDo.setProdId(borrowList.getProdId());
        //用户未登录
        if (person.getLoginTime() == null && STATUS_APLLY.equals(borrowList.getBorrStatus())) {
            logger.info("该用户手机号为phone=" + person.getPhone() + ",当前所处节点状态状态 node=" + NodeEnum.UNLOGIN_NODE.getNode() + "");
            nodeDo.setNode(NodeEnum.UNLOGIN_NODE.getNode());
            return ResponseDo.newSuccessDo(nodeDo);
        }
        //判断用户是否设置支付密码
        if (StringUtils.isEmpty(person.getPayPassword()) && STATUS_APLLY.equals(borrowList.getBorrStatus())
                && CodeReturn.PRODUCT_TYPE_CODE_CARD.equals(product.getProductTypeCode())) {
            logger.info("该用户手机号为phone=" +  person.getPhone() + ",当前所处节点状态状态 node=" + NodeEnum.SETPAYPWD_NODE.getNode() + "");
            nodeDo.setNode(NodeEnum.SETPAYPWD_NODE.getNode());
            return ResponseDo.newSuccessDo(nodeDo);
        }
        //TODO:更改公司主体先判断是否需要删除该用户所有银行卡
        verifyService.verifyBankBody(String.valueOf(person.getId()));
        //用户未银行卡认证
        Bank bank = bankMapper.selectPrimayCardByPerId(String.valueOf(person.getId()));
        if (bank == null) {
            logger.info("该用户手机号为phone=" +  person.getPhone() + ",当前所处节点状态状态 node=" + NodeEnum.BANKCARD_NODE.getNode() + "");
            nodeDo.setNode(NodeEnum.BANKCARD_NODE.getNode());
            return ResponseDo.newSuccessDo(nodeDo);
        } else {
            //第二次借款银行卡已认证直接将合同改为BS002
            ResponseDo<BorrowList> newBorr = updateBorrowStatus(String.valueOf(person.getId()), STATUS_WAIT_SIGN);
            if (200 == newBorr.getCode()) {
                borrowList = newBorr.getData();
            } else {
                return ResponseDo.newFailedDo(newBorr.getInfo());
            }

        }
        //用户状态为待签约
        if (STATUS_WAIT_SIGN.equals(borrowList.getBorrStatus())) {
            logger.info("该用户手机号为phone=" +  person.getPhone() + ",当前所处节点状态状态 node=" + NodeEnum.UNSIGN_NODES.getNode() + "");
            nodeDo.setNode(NodeEnum.UNSIGN_NODES.getNode());
            return ResponseDo.newSuccessDo(nodeDo);
        }
        //跳转详情页
        if (STATUS_TO_REPAY.equals(borrowList.getBorrStatus())
                || CodeReturn.STATUS_LATE_REPAY.equals(borrowList.getBorrStatus())
                || CodeReturn.STATUS_COM_PAYING.equals(borrowList.getBorrStatus())) {
            logger.info("该用户手机号与合同号" +  person.getPhone() + ",当前所处节点状态状态 node=" + NodeEnum.DETAILS_NODES.getNode() + "");
           nodeDo.setNode(NodeEnum.DETAILS_NODES.getNode());
            return ResponseDo.newSuccessDo(nodeDo);
        }
        logger.info("该用户手机号为phone=" +  person.getPhone() + ",当前所处节点状态状态 node=" + NodeEnum.ERROR_NODES.getNode() + "");
        nodeDo.setNode(NodeEnum.ERROR_NODES.getNode());
        return ResponseDo.newSuccessDo(nodeDo);
    }

    @Override
    public ResponseDo<BorrowList> updateBorrowStatus(String perId, String rlStatus) {
        //获取用户当前合同
        BorrowList nowBorr = borrowListMapper.selectNow(Integer.parseInt(perId));
        logger.info("银行卡验证后用户当前合同状态 nowBorr" + nowBorr);
        if (nowBorr == null) {
            return ResponseDo.newFailedDo("你当前尚未申请，请返回申请");
        }
        //申请中变为待签约
        if (STATUS_APLLY.equals(nowBorr.getBorrStatus())) {
            nowBorr.setBorrStatus(rlStatus);
            borrowListMapper.updateByPrimaryKeySelective(nowBorr);
            AsyncExecutor.syncExecute(new PostAsync<>(nowBorr, borrowUrl, borrowListMapper));
            return ResponseDo.newSuccessDo(nowBorr);
        } else {
            return ResponseDo.newSuccessDo(nowBorr);
        }
    }

    @Override
    public ResponseDo<DetailsDo> getDetails(String phone, String borrNum) {
        Person person = personMapper.getPersonByPhone(phone);
        if (person == null) {
            return ResponseDo.newFailedDo("该用户不存在");
        }
        DetailsDo detail = borrowListMapper.getDetails(borrNum);

        //根据产品存入不同的属性
        detail.setBorrName(detail.getBorrStatus(), detail.getProdType());
        if (detail.getPlanRepay() == 0 || detail.getAmountSurplus() == 0) {
            //调用风控试算接口
            ResponseDo<SettlementResult> planInfo = agreementService.getPlanInfo(detail.getBorrId());
            if (200 == planInfo.getCode()) {
                JSONObject obj = JSONObject.parseObject(planInfo.getData().getModel());
                detail.setPlanRepay(obj.getFloat("plan_repay"));
                detail.setAmountSurplus(obj.getFloat("amount_surplus"));
            }
        }
        return ResponseDo.newSuccessDo(detail);
    }


    @Override
    public ResponseDo<RepayInfoDo> jumpRepay(String perId, String borrId) {
        logger.info("用户付款页面跳转请求参数 perId = " + perId + "borrId=" + borrId);
        Person p = personMapper.selectByPrimaryKey(Integer.parseInt(perId));
        if (p == null) {
            return ResponseDo.newFailedDo("用户不存在");
        }
        BorrowList bl = borrowListMapper.selectByPrimaryKey(Integer.parseInt(borrId));
        if (bl == null || !(STATUS_TO_REPAY.equals(bl.getBorrStatus())
                || CodeReturn.STATUS_LATE_REPAY.equals(bl.getBorrStatus()))) {
            return ResponseDo.newFailedDo("当前合同状态不正确，无法还款");
        }
        //获取扣款手续费
        String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "1");
        //获取用户银行卡列表
        List<Bank> bank = bankMapper.selectAllBanks(perId);
        RepayInfoDo info = new RepayInfoDo();
        info.setBorrId(bl.getId());
        info.setPerId(bl.getPerId());
        info.setAmountSurplus(bl.getAmountSurplus());
        info.setBank(bank);
        info.setBorrNum(bl.getBorrNum());
        info.setPhone(p.getPhone());
        info.setFee(StringUtils.isEmpty(fee) ? 0L : new BigDecimal(fee).floatValue());
        return ResponseDo.newSuccessDo(info);
    }

    @Override
    public ResponseDo<?> synchronousBorrow(BorrowList borrowList) {
        //获取清结算响应状态
        logger.info("清结算状态改变 borrowList"+borrowList);
        borrowList = borrowListMapper.selectByBorrNum(borrowList.getBorrNum());
        AsyncExecutor.syncExecute(new PostAsync<>(borrowList, borrowUrl, borrowListMapper));
        return ResponseDo.newSuccessDo();
    }

    @Override
    public ResponseDo<PaymentInfoDo> paymentInfo(PaymentInfoVo vo) {
        logger.info("获取用户付款页面信息 PaymentInfoVo = " + vo);
        //获取用户选定银行卡信息
        PaymentInfoDo info = bankMapper.getPaymentInfoDo(vo.getPerId(), vo.getBankId());
        //获取用户合同信息
        BorrowList borrowList = borrowListMapper.selectByPrimaryKey(vo.getBorrId());
        //获取客服电话
        String servicePhone = codeValueMapper.selectCodeByType("service_phone");
        info.setBorrId(borrowList.getId());
        info.setBorrNum(borrowList.getBorrNum());
        info.setProdId(borrowList.getProdId());
        info.setPerId(vo.getPerId());
        info.setAmountSurplus(vo.getAmountSurplus());
        info.setActualAmount(vo.getActualAmount());
        info.setServicePhone(servicePhone);
        return ResponseDo.newSuccessDo(info);
    }

    /**
     * 获取合同信息
     *
     * @param borrId
     * @return
     */
    @Override
    public Map<String, Object> getContractInfoByBorrId(Integer borrId) {
        String phone = borrowListMapper.getPersonPhoneByBorrId(borrId);
        String param = "phone=" + phone;
        String result = HttpUrlPost.sendGet(contractUrl, param);

        Date date = new Date();
        BorrowList borrowList = borrowListMapper.selectByPrimaryKey(borrId);
        Map<String, Object> map = JSONObject.parseObject(result, Map.class);
        map.put("borrNum", borrowList.getBorrNum());
        map.put("createDate", DateFormatUtils.format(date, "yyyy年MM月dd日"));

        // 查看是否是随心贷
        if("pay_money".equals(borrowList.getProdType())){
            //调用风控试算接口
            ResponseDo<SettlementResult> planInfo = agreementService.getPlanInfo(borrId);
            if (200 == planInfo.getCode()) {
                JSONObject obj = JSONObject.parseObject(planInfo.getData().getModel());
                map.put("planRepayDate", DateFormatUtils.format(obj.getDate("planrepay_date"), "yyyy年MM月dd日"));
                map.put("planRepay",obj.getBigDecimal("plan_repay").setScale(2, RoundingMode.DOWN).toPlainString());
                map.put("interestSum", obj.getBigDecimal("interest_sum").setScale(2,RoundingMode.DOWN).toPlainString());
                map.put("borrAmount",obj.getBigDecimal("capitalSum").setScale(2,RoundingMode.DOWN).toPlainString());
            }
            Bank bank = bankMapper.selectPrimayCardByPerId(String.valueOf(borrowList.getPerId()));
            if(bank!=null) {
                map.put("bankNum", bank.getBankNum());
                map.put("bankName", bank.getBankName());
            }
            map.put("term",borrowList.getTermday()*borrowList.getTermNum());
            map.put("year",DateFormatUtils.format(date,"yyyy"));
            map.put("month",DateFormatUtils.format(date,"MM"));
            map.put("day",DateFormatUtils.format(date,"dd"));
            // 判断公司主体2018年11月14日 16:14:39
            Product product = productMapper.selectByPrimaryKey(borrowList.getProdId());
            map.put("payAmount",product.getPayAmount().setScale(2, RoundingMode.DOWN));
            map.put("serviceFee",product.getServiceFee().setScale(2, RoundingMode.DOWN));
            map.put("maximumAmount",new BigDecimal(product.getMaximumAmount()).setScale(2, RoundingMode.DOWN));
            map.put("yearReat", Math.round((product.getInterestRate().doubleValue() * 360 / 10 * 100) * 100) / 100 + "");
            if(product.getCompanyBody() == 1 ){
                map.put("view","agreement/borrowAgreement2");
            }else{
                map.put("view","agreement/borrowAgreementXiAn");
            }
        }else{
            map.put("view","agreement/saleContract");
        }
        return map;
    }

    /**
     * 通过合同id更新合同状态
     *
     * @param borrowNum
     * @param status
     * @return
     */
    @Override
    public String updateBorrowStatusByBorrowNum(String borrowNum, String status) {
        borrowListMapper.updateStatusByBorrNum(borrowNum, status);
        return "SUCCESS";
    }

    /**
     * 查看我的合同
     * @param borrId
     * @return
     */
    @Override
    public Map<String, String> getContractImageByBorrId(Integer borrId) {
        Map<String,String> map = contractMapper.selectContractByBorrId(borrId);
        if("pay_card".equals(map.get("prodType"))){
            map.put("title","销售合同");
        }else{
            map.put("title","借款协议");
        }
        return map;
    }

    /**
     * 分配审核人方法
     *
     * @param borrId
     * @param emp_num
     * @return
     */
    private int setReviewer(Integer borrId, String emp_num) {
        //幂等操作
        String redisResult = jedisCluster.get(RedisConst.REVIEW_KEY + borrId);
        logger.info(String.format("【setReviewer】jedisCluster get key: %s, result: %s, borrId: %s", RedisConst.REVIEW_KEY + borrId, redisResult, borrId));
        if (!StringUtils.isEmpty(redisResult)) {
            logger.error("直接返回，重复数据审核分配" + borrId);
            return 0;
        }

        String setNX = jedisCluster.set(RedisConst.REVIEW_KEY + borrId, borrId.toString(), "NX", "EX", 30 * 60);
        logger.info(String.format("【setReviewer】jedisCluster set key: %s, result: %s, borrId: %s", RedisConst.REVIEW_KEY + borrId, setNX, borrId));
        if (!"OK".equals(setNX)) {
            logger.error("直接返回，重复数据审核分配" + borrId);
            return 0;
        }

        //分配审核人之前先看有没有审核人 如果有 直接返回1
        if (reviewMapper.selectReview(borrId) > 0) {
            return 1;
        }
        //新增审核表记录
        Integer sum = reviewMapper.reviewSum();
        if (sum == null) {
            sum = 0;
        }
        //获得所有审核人的员工编号
        List<String> reviewerList = reviewersMapper.selectEmployNum();
        //给该borrowList设置审核人
        int turn = sum % reviewerList.size();

        Review review = new Review();
        review.setBorrId(borrId);
        review.setReviewType("1");
        //如果员工编号传空，自动分配
        if (StringUtils.isEmpty(emp_num)) {
            review.setEmployNum(reviewerList.get(turn));
        } else {
            review.setEmployNum(emp_num);
        }
        return reviewMapper.insertSelective(review);
    }

    public void signRobot(BorrowList borrowList) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        logger.info("当前首单审核时间为：" + currentHour + "时");
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String beginTime = codeValueMapper.selectCodeByType("baikelu_audit_begin_time");
        String endTime = codeValueMapper.selectCodeByType("baikelu_audit_end_time");
        Boolean flagBeginTime = Integer.valueOf(sdf.format(new Date()).toString()) >= Integer.valueOf(beginTime);
        Boolean flagEndTime = Integer.valueOf(sdf.format(new Date()).toString()) <= Integer.valueOf(endTime);
        logger.info("flagBeginTime:" + flagBeginTime + "---------flagEndTime:" + flagEndTime);
        if (flagBeginTime && flagEndTime) {
            logger.info("进入机器人首单审核");
            String url = PropertiesReaderUtil.read("third", "robotUrl");
            Map<String, String> map = new HashMap<>();
            map.put("borrId", borrowList.getId().toString());
            HttpUrlPost.sendPost(url, map);
        }
    }

    @Override
    public void synchBorrowStatus() {
        List<BorrowList> borrowList = borrowListMapper.getBorrowByFinalStatus();
        if (borrowList != null && borrowList.size() > 0) {
            borrowList.forEach(v -> AsyncExecutor.syncExecute(new PostAsync<>(v, borrowUrl, borrowListMapper)));
        }
    }

    @Override
    public void synchBorrowStatusOverdue() {
        List<BorrowList> borrowList = borrowListMapper.getBorrowByOverdue();
        if (borrowList != null && borrowList.size() > 0) {
            borrowList.forEach(v -> AsyncExecutor.syncExecute(new PostAsync<>(v, borrowUrl, borrowListMapper)));
        }
    }

    /**
     *  验证用户每日申请次数
     * @param phone 手机号
     */
    private boolean validApplyLimit(String phone) {
        //判断用户每天申请次数
        Long size = jedisCluster.hlen(RedisConst.DC_LOAN_APPLY_LIMIT_KEY);
        if (size == 0){
            jedisCluster.hset(RedisConst.DC_LOAN_APPLY_LIMIT_KEY,phone,"1");
            //设置key过期时间
            jedisCluster.expire(RedisConst.DC_LOAN_APPLY_LIMIT_KEY, DateUtil.getCurrentTimeToTomorrow());
        }else {
            if (0 == jedisCluster.hsetnx(RedisConst.DC_LOAN_APPLY_LIMIT_KEY,phone,"1")){
                //获取用户最大次数 项目初始化时放入
                String limit = jedisCluster.get(RedisConst.DC_LOAN_APPLY_MAX_NUMBER_KEY);
                if (limit == null) {
                    String applyLimit = codeValueMapper.selectCodeByType("apply_limit");
                    jedisCluster.setnx(RedisConst.DC_LOAN_APPLY_MAX_NUMBER_KEY, applyLimit);
                    limit = StringUtils.isEmpty(applyLimit) ? "3" : applyLimit;
                }
                if (Long.parseLong(limit) < jedisCluster.hincrBy(RedisConst.DC_LOAN_APPLY_LIMIT_KEY,phone,1)){
                    logger.error("该用户已超过每日限制次数 phone = "+phone);
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal(1);
        bigDecimal.setScale(2);
        System.out.println(bigDecimal);
    }
}
