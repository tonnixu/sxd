package com.jhh.dc.loan.service.app;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.dc.loan.api.app.UserService;
import com.jhh.dc.loan.api.constant.StateCode;
import com.jhh.dc.loan.api.entity.ForgetPayPwdVo;
import com.jhh.dc.loan.api.entity.LoginVo;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.common.util.Base64;
import com.jhh.dc.loan.common.util.CodeReturn;
import com.jhh.dc.loan.common.util.MD5Util;
import com.jhh.dc.loan.common.util.RedisConst;
import com.jhh.dc.loan.common.util.rsa.RSAUtil;
import com.jhh.dc.loan.constant.Constant;
import com.jhh.dc.loan.entity.app.Bank;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.app.NoteResult;
import com.jhh.dc.loan.entity.app.Person;
import com.jhh.dc.loan.entity.app_vo.BorrowListVO;
import com.jhh.dc.loan.entity.app_vo.JdCardInfoVO;
import com.jhh.dc.loan.entity.app_vo.JdCardKeyInfo;
import com.jhh.dc.loan.entity.common.Constants;
import com.jhh.dc.loan.entity.manager.Feedback;
import com.jhh.dc.loan.entity.manager.Msg;
import com.jhh.dc.loan.entity.manager.MsgTemplate;
import com.jhh.dc.loan.entity.manager.RepaymentPlan;
import com.jhh.dc.loan.mapper.app.BankMapper;
import com.jhh.dc.loan.mapper.app.BorrowListMapper;
import com.jhh.dc.loan.mapper.app.JdCardInfoMapper;
import com.jhh.dc.loan.mapper.app.PersonMapper;
import com.jhh.dc.loan.mapper.manager.FeedbackMapper;
import com.jhh.dc.loan.mapper.manager.MsgMapper;
import com.jhh.dc.loan.mapper.manager.MsgTemplateMapper;
import com.jhh.dc.loan.mapper.product.ProductCompanyExtMapper;
import com.jhh.dc.loan.util.BorrowUtil;
import com.jhh.pay.driver.pojo.BankInfo;
import com.jhh.pay.driver.pojo.PayRequest;
import com.jhh.pay.driver.pojo.PayResponse;
import com.jhh.pay.driver.service.TradeService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;
import com.jhh.dc.loan.api.constant.Constants.*;
/**
 * @author xingmin
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String WHITE_LIST_KEY = Constants.YM_ADMIN_SYSTEN_KEY + Constants.WHITELIST_PERID;


    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private FeedbackMapper feedMapper;

    @Autowired
    private MsgMapper msgMapper;

    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private JdCardInfoMapper jdCardInfoMapper;

    @Autowired
    private ProductCompanyExtMapper productCompanyExtMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private VerifyServiceImpl verifyService;

    @Value("${ras_private_key_path}")
    private String privateKeyPath;

    private RSAPrivateKey rsaPrivateKey;

    @PostConstruct
    public void initPrivateKey(){
        String privateKey = RSAUtil.readKeyFromFile(privateKeyPath);
        if(privateKey == null){
            throw new RuntimeException("读取私钥失败:私钥路径"+privateKeyPath);
        }
        rsaPrivateKey = RSAUtil.readPrivateKeyFromString(privateKey);
    }




    @Override
    public ResponseDo<Person> selectPersonById(String userId) {
        try {
            Person p = personMapper.selectByPrimaryKey(Integer.parseInt(userId));
            return ResponseDo.newSuccessDo(p);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDo.newFailedDo("系统正在开小差,请稍候");
        }
    }


    @Override
    public ResponseDo<?> userFeedBack(Feedback feed) {
        String setnx = jedisCluster.set(RedisConst.APP_USER_FEEDBACK + feed.getPerId(), "on", "NX", "EX", 60 * 5);
        if (!"OK".equals(setnx)) {
           return ResponseDo.newFailedDo("5分钟之内不能重复反馈");
        }
        try {
            feedMapper.insertSelective(feed);
            return ResponseDo.newSuccessDo("反馈成功!");
        } catch (Exception e) {
            e.printStackTrace();
            jedisCluster.del(RedisConst.APP_USER_FEEDBACK + feed.getPerId());
            return ResponseDo.newFailedDo("系统错误!");
        }
    }

    @Override
    public String setMessage(String userId, String templateId, String params) {
        JSONObject obj = new JSONObject();
        logger.info("发送站内信参数:userId:" + userId + ",templateId:" + templateId + ",params:" + params + "");
        try {
            String[] cc = Arrays.stream(params.split(",")).map(t -> isNumeric(t) ? String.format("%.2f", Double.valueOf(t)) : t).toArray(String[]::new);
            // 定义最后的消息内容
            String dd = "";
            // 获取消息模版
            MsgTemplate msgTemplate = msgTemplateMapper
                    .selectByPrimaryKey(Integer.parseInt(templateId));
            if (null != msgTemplate) {

                if ("1".equals(msgTemplate.getStatus())) {
                    // 获取模版的内容
                    String ll = msgTemplate.getContent();
                    // 获取模版的标题
                    String title = msgTemplate.getTitle();
                    // 分割模版内容
                    String[] aa = ll.split("\\{");
                    // 将模版内容和参数拼接成最后的消息内容
                    for (int i = 0; i < aa.length; i++) {
                        String[] bb = aa[i].split("}");
                        if (bb.length > 1) {
                            dd += cc[i - 1] + bb[1];
                        } else {
                            dd += aa[i];
                        }
                    }
                    Msg msg = new Msg();
                    msg.setContent(dd);
                    msg.setTitle(title);
                    msg.setPerId(Integer.parseInt(userId));
                    msg.setStatus("n");
                    msg.setType(1);
                    msg.setCreateTime(new Date());
                    msgMapper.insertSelective(msg);
                    obj.put("code", CodeReturn.success);
                    obj.put("info", "消息发送成功");
                    obj.put("data", dd);
                    logger.info("站内信发送成功！userId:" + userId + "");
                } else {
                    obj.put("code", CodeReturn.fail);
                    obj.put("info", "模版已失效");
                    logger.info("站内信模版已失效！");
                }
            } else {
                obj.put("code", CodeReturn.fail);
                obj.put("info", "模版不存在");
                logger.info("站内信模版不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "消息发送失败,请检查参数和模版是否匹配！");
            logger.info("站内信发送失败,请检查参数和模版是否匹配！");
        }
        return obj.toString();
    }


    /**
     * 利用正则表达式判断字符串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String setRedisData(String key, int time, String data) {
        return jedisCluster.setex(key, time, data);
    }

    @Override
    public String queryRedisData(String key) {
        return jedisCluster.get(key);
    }


    /**
     * 处理还款计划
     *
     * @param repaymentPlans
     * @param borrowList
     * @return
     */
    private List<RepaymentPlan> handleRepaymentPlans(List<RepaymentPlan> repaymentPlans, BorrowList borrowList) {
        repaymentPlans.forEach(repaymentPlan -> {
            if (repaymentPlan.getIsLast() == null) {
                return;
            }
            if (repaymentPlan.getIsLast() != 1) {
                repaymentPlan.setSurplusAmount(repaymentPlan.getSurplusRentalAmount().add(repaymentPlan.getSurplusPenalty()));
                repaymentPlan.setPaidAmount(repaymentPlan.getRentalAmount().add(repaymentPlan.getPenalty()).subtract(repaymentPlan.getSurplusAmount()));
                return;
            }

            // 不退押金, 押金置为0
            if (borrowList.getNoDepositRefund() != null && borrowList.getNoDepositRefund() == 1) {
                borrowList.setDepositAmount(0F);
            }

            // 结清状态, 押金置为0
            if (StringUtils.equals(Constant.STATUS_PAY_BACK, borrowList.getBorrStatus()) || StringUtils.equals(Constant.STATUS_DELAY_PAYBACK, borrowList.getBorrStatus()) || StringUtils.equals(Constant.STATUS_EARLY_PAYBACK, borrowList.getBorrStatus())) {
                borrowList.setDepositAmount(0F);
            }

            repaymentPlan.setSurplusAmount(repaymentPlan.getSurplusRentalAmount().add(repaymentPlan.getSurplusPenalty()).add(new BigDecimal(borrowList.getSurplusRansomAmount())).subtract(new BigDecimal(borrowList.getDepositAmount())));
            repaymentPlan.setPaidAmount(repaymentPlan.getRentalAmount().add(repaymentPlan.getPenalty()).add(new BigDecimal(borrowList.getRansomAmount())).subtract(repaymentPlan.getSurplusAmount()).subtract(new BigDecimal(borrowList.getDepositAmount())));
        });

        return repaymentPlans;
    }


    /**
     * 查询快捷支付绑定状态并发送验证码
     *
     * @param phone
     * @param bankNum
     * @param perId
     * @return
     */
    @Override
    public NoteResult queryBindAndSendMsg(String phone, String bankNum, String perId) {
        NoteResult noteResult;

        Person person = personMapper.selectByPrimaryKey(Integer.parseInt(perId));

        //调用支付中心查询绑卡及发送验证码接口
        PayRequest payRequest = new PayRequest();
        BorrowList nowBorr = borrowListMapper.selectNow(Integer.parseInt(perId));
        String appId;
        if (BorrowUtil.borrowFinalState(nowBorr)){
            appId = productCompanyExtMapper.selectValueByCompany(PayStyleConstants.XIANLOAN_COMPANY_BODY_FALG, PayStyleConstants.DC_BIND_CARD_APPID);
        } else{
            appId = productCompanyExtMapper.selectValueByProductId(nowBorr.getProdId(),PayStyleConstants.DC_BIND_CARD_APPID);
        }
        payRequest.setAppId(Integer.parseInt(appId));

        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankCard(bankNum);
        bankInfo.setBankMobile(phone);
        bankInfo.setPersonalName(person.getName());
        payRequest.setBankInfo(bankInfo);

        Long orderNum = jedisCluster.incr(RedisConst.HELI_PAY_MSG_ORDER_NUM);
        Map<String, Object> extension = new HashMap<>();
        extension.put("user_id", person.getId());
        extension.put("id_card_no", person.getCardNum());
        payRequest.setExtension(extension);

        String date = DateFormatUtils.format(new Date(), "yyyyMMdd");
        payRequest.setOrderNo("dc" + date + orderNum.toString());
        logger.info("开始调用查询是否绑卡及发送短信接口, 参数为 -> " + payRequest);
        PayResponse payResponse = tradeService.sendMsg(payRequest);
        noteResult = NoteResult.SUCCESS_RESPONSE();
        logger.info("查询绑卡及发送短信接口返回参数为 -> " + payResponse);
        if (Constants.CODE_200.equals(payResponse.getCode()) && !"NONEED".equals(payResponse.getState())) {
            String channelKey = payResponse.getChannelKey();
            if (StringUtils.isNotEmpty(channelKey)) {
                Map<String, String> data = new HashMap<>();
                data.put("msgChannel", channelKey);
                noteResult.setData(data);
                //在redis中记录快捷支付验证码标记
                String valiCodeKey = RedisConst.VALIDATE_CODE + RedisConst.SEPARATOR + phone;
                jedisCluster.setex(valiCodeKey, 30 * 60, RedisConst.HELI_PAY_MSG_ORDER_NUM);
                logger.info("redis key:" + valiCodeKey + ",在redis记录本次快捷支付验证码" + RedisConst.HELI_PAY_MSG_ORDER_NUM);
                //在redis中记录快捷支付验证码渠道
                String valiCodeChannelKey = RedisConst.VALIDATE_CODE_CHANNEL + RedisConst.SEPARATOR + phone;
                jedisCluster.setex(valiCodeChannelKey, 30 * 60, channelKey+","+payResponse.getOrderNo());
                logger.info("在redis记录本次快捷支付短信渠道:" + channelKey);
            }
        } else {
            String valiCodeStatusKey = new StringBuilder(RedisConst.VALIDATE_CODE_STATUS).append(RedisConst.SEPARATOR).append(phone).toString();
            jedisCluster.setex(valiCodeStatusKey,2*60, payResponse.getState());
            noteResult = NoteResult.FAIL_RESPONSE(payResponse.getMsg());
            logger.error("调用查询是否绑卡及发送短信接口失败:" + payResponse.getMsg());
        }
        return noteResult;
    }


    @Override
    public Person selectByPhone(String phone) {
        return personMapper.getPersonByPhone(phone);
    }



    @Override
    public ResponseDo<?> updatePersonInfo(Person person) {
        Person p = personMapper.getPersonByPhone(person.getPhone());
        if (p == null) {
            return ResponseDo.newFailedDo("该手机号不存在,请先注册");
        }
        personMapper.updateByPrimaryKeySelective(person);
        return ResponseDo.newSuccessDo();
    }

    @Override
    public ResponseDo<Person> login(LoginVo vo) {

        ResponseDo<Person> responseDo = verifyCode(vo.getTimestamp(),vo.getPhone(),vo.getGraphiCode(),vo.getValidateCode());
        if (responseDo != null) return responseDo;
        //获取用户
        Person p = personMapper.getPersonByPhone(vo.getPhone());
        p.setLoginTime(new Date());
        personMapper.updateByPrimaryKeySelective(p);
        return ResponseDo.newSuccessDo(p);
    }

    @Override
    public ResponseDo<?> forgetPayPwd(ForgetPayPwdVo vo) {

        ResponseDo<Person> responseDo = verifyCode(vo.getTimestamp(),vo.getPhone(),vo.getGraphiCode(),vo.getValidateCode());
        if (responseDo != null) return responseDo;
        //获取用户
        return ResponseDo.newSuccessDo();
    }

    private ResponseDo<Person> verifyCode(String timestamp,String phone,String graphiCode,String validateCode) {
        //验证图形码
        String correctGraphiCode = queryRedisData("h5VerifyCode" + timestamp);
        logger.info("该用户phone" + phone + "的图形验证码为 correctGraphiCode" + correctGraphiCode);
        if (!graphiCode.equalsIgnoreCase(correctGraphiCode)) {
            return new ResponseDo<>(StateCode.GRAPHICODE_ERROR_CODE, StateCode.GRAPHICODE_ERROR__MSG);
        }
        /* 验证短信验证码 */
        if (!validateCode.equals(queryRedisData("h5msgCode"+phone+""))){
             return new ResponseDo<>(StateCode.SMS_VALID_FAIL_CODE,StateCode.SMS_VALID_FAIL_MSG);
        }
        return null;
    }

    /**
     * 根据用户id获取我的卡包信息
     * @param phone
     * @return
     */
    @Override
    public List<JdCardInfoVO> getMyCardsByPhone(String phone) {
        List<JdCardInfoVO> data = jdCardInfoMapper.selectCardInfoByPhone(phone);
        return data;
    }
    /**
     * 更新京东卡字段并获取卡密信息
     * @param jdCardId
     * @param phone
     * @param password
     * @return
     */
    @Override
    public ResponseDo<JdCardKeyInfo> updateFetchAndGetCardNum(Integer jdCardId, String phone, String password) throws Exception {

        // 校验用户密码
        Person person = personMapper.getPersonByPhone(phone);
        if(person==null){
            return ResponseDo.newFailedDo("用户不存在!");
        }else{
            String passwrod = MD5Util.encodeToMd5(password);
            if(!passwrod.equals(person.getPayPassword())){
                return ResponseDo.newFailedDo("密码不正确!");
            }
        }

        // 更新首次获取密码时间
        jdCardInfoMapper.updateFetchTimeById(jdCardId);

        JdCardKeyInfo jdCardKeyInfo = jdCardInfoMapper.getCardKeyInfoByCardId(jdCardId);
        // 解密京东卡密
        String jdPassword = jdCardKeyInfo.getPassword();
        jdPassword = RSAUtil.decryptByPrivateKey(Base64.decode(jdPassword),rsaPrivateKey);
        jdCardKeyInfo.setPassword(jdPassword);
        return ResponseDo.newSuccessDo(jdCardKeyInfo);
    }

    /**
     * 根据用户手机号获取账单信息
     * @param phone
     * @return
     */
    @Override
    public List<BorrowListVO> getBorrowListByPhone(String phone) {
        List<BorrowListVO> list = borrowListMapper.getBorrListVoByPhone(phone);
        return list;
    }
    @Override
    public ResponseDo<?> userSetPassword(String phone, String paypwd, String confirmPaypwd) {
        //判断密码格式
        if (!paypwd.matches("\\d{6}") && !paypwd.matches("\\d{6}")){
            return ResponseDo.newFailedDo("密码格式不正确，请重新输入");
        }
        if (!paypwd.equals(confirmPaypwd)){
            return ResponseDo.newFailedDo("密码不一致，请确认后提交");
        }
        Person person = personMapper.getPersonByPhone(phone);
        if (person == null){
            return ResponseDo.newFailedDo("该用户不存在");
        }
        person.setPayPassword(MD5Util.encodeToMd5(paypwd));
        personMapper.updateByPrimaryKeySelective(person);
        return ResponseDo.newSuccessDo();
    }

    /**
     * 根据用户id获取账单信息
     * @param personId
     * @return
     */
    @Override
    public List<BorrowListVO> getBorrowListByPersonId(Integer personId) {
        return borrowListMapper.getBorrowListVOByPersonId(personId);
    }

    @Override
    public ResponseDo<List<Bank>> getBankManagement(String perId) {
        //TODO：因更换公司主体先判断用户当前银行卡是否作废
        verifyService.verifyBankBody(perId);
        List<Bank> banks = bankMapper.selectAllBanks(perId);
        return ResponseDo.newSuccessDo(banks);
    }

}