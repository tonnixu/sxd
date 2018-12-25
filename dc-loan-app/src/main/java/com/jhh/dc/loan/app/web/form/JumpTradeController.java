package com.jhh.dc.loan.app.web.form;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.dc.loan.api.app.LoanService;
import com.jhh.dc.loan.api.app.UserService;
import com.jhh.dc.loan.api.constant.StateCode;
import com.jhh.dc.loan.api.entity.*;
import com.jhh.dc.loan.app.common.constant.AccountValidatorUtil;
import com.jhh.dc.loan.app.common.exception.CommonException;
import com.jhh.dc.loan.app.web.exception.ExceptionPageController;
import com.jhh.dc.loan.entity.app_vo.SignInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 2018/7/23.
 */
@Controller
@RequestMapping("/form")
@Slf4j
public class JumpTradeController extends ExceptionPageController {

    @Reference
    private UserService userService;

    @Reference
    private LoanService loanService;

    @ApiOperation(value = "跳转详情页面",notes = "跳转详情页面,A公司入口之一",hidden = true)
    @RequestMapping("/jumpDetails/{phone}/{borrNum}")
    public String jumpDetails(@PathVariable("phone") String phone, @PathVariable("borrNum") String borrNum,
                             @RequestParam String prodType,Map<String,Object> map, HttpServletRequest request) throws CommonException {
        //拦截器存入cookie用
        request.setAttribute("phone",phone);
        request.setAttribute("prodType",prodType);
        if (!AccountValidatorUtil.isMobile(phone)) {
            throw new CommonException(StateCode.PHONE_ERROR,StateCode.PHONE_ERROR_MSG);
        }
        ResponseDo<DetailsDo> details = loanService.getDetails(phone,borrNum);
        if (200 == details.getCode()){
            request.setAttribute("prodType",details.getData().getProdType());
            map.put("details",details.getData());
        }else {
            throw new CommonException(201,details.getInfo());
        }
        return "trade/details";
    }

    @ApiOperation(value = "银行卡认证跳转签约页面",hidden = true)
    @RequestMapping("/jumpSign/{phone}")
    public String jumpSign(@PathVariable("phone") String phone,Map<String, Object> map){
        //获取签约页面信息
        SignInfo signInfo = loanService.getSignInfo(phone);
        map.put("signInfo",signInfo);
        return "trade/sign";
    }

    @ApiOperation(value = "跳转还款页面",notes = "跳转还款页面",hidden = true)
    @RequestMapping("/jumpRepay/{perId}/{borrId}")
    public String jumpRepay(@PathVariable("perId") String perId, @PathVariable("borrId") String borrId,Map<String,Object> map) throws CommonException {
        ResponseDo<RepayInfoDo> bankResponseDo = loanService.jumpRepay(perId, borrId);
        if (bankResponseDo != null && 200 == bankResponseDo.getCode()){
            map.put("info",bankResponseDo.getData());
        }else {
            throw new CommonException(201,bankResponseDo == null ? "服务器开小差了，请稍候再试" : bankResponseDo.getInfo());
        }
        return "trade/repay";
    }

    @ApiOperation(value = "付款页面",notes = "跳转付款页面",hidden = true)
    @RequestMapping("/paymentInfo")
    public String payment(PaymentInfoVo vo, Map<String,Object> map){
        ResponseDo<PaymentInfoDo> paymentInfo = loanService.paymentInfo(vo);
        if (200 == paymentInfo.getCode()){
            map.put("info",paymentInfo.getData());
        }
        return "trade/payment";
    }

    @ApiOperation(value = "支付处理中",notes = "支付成功处理中页面",hidden = true)
    @RequestMapping("/paying/{registerPhone}/{borrNum}")
    public String feedback(@PathVariable("registerPhone") String registerPhone, @PathVariable("borrNum") String borrNum, Map<String,Object> map) {
        map.put("registerPhone",registerPhone);
        map.put("borrNum",borrNum);
        return "status/paying";
    }

}
