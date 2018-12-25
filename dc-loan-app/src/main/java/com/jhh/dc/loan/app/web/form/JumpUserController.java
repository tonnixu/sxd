package com.jhh.dc.loan.app.web.form;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.dc.loan.api.app.LoanService;
import com.jhh.dc.loan.api.app.UserService;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.entity.UserNodeDo;
import com.jhh.dc.loan.api.enums.NodeEnum;
import com.jhh.dc.loan.app.common.constant.AccountValidatorUtil;
import com.jhh.dc.loan.app.common.exception.CommonException;
import com.jhh.dc.loan.app.web.exception.ExceptionPageController;
import com.jhh.dc.loan.entity.app.Bank;
import com.jhh.dc.loan.entity.app.Person;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 2018/7/23.
 */
@Controller
@RequestMapping("/form")
@Slf4j
public class JumpUserController extends ExceptionPageController {


    @Reference
    private UserService userService;

    @Reference
    private LoanService loanService;

    @Autowired
    private JedisCluster jedisCluster;

    @ApiOperation(value = "A公司申请跳转B公司统一入口",notes = "生成用户,借款合同，页面跳转等所有操作",hidden = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "borrNum",value = "合同编号,暂时无用，为扩展做准备" ),
            @ApiImplicitParam(name = "reviewer",value = "审核人")
    })
    @RequestMapping("/applyBorrow/{phone}/{productId}")
    public String applyBorrow(@PathVariable("phone") String phone, @PathVariable("productId") String productId,
                              String borrNum,@RequestParam String prodType, String reviewer, HttpServletRequest request) throws CommonException {
        //拦截器存入cookie用
        request.setAttribute("phone",phone);
        request.setAttribute("prodType",prodType);
        if (!AccountValidatorUtil.isMobile(phone)) {
            throw new CommonException(201,"手机号格式错误");
        }
        ResponseDo<UserNodeDo> result = loanService.applyBorrow(phone, productId,borrNum,reviewer);
        if (200 == result.getCode()){
            //判断用户跳转页面
            request.setAttribute("prodType",result.getData().getProdType());
            request.setAttribute("info",result.getData());
            if (NodeEnum.UNLOGIN_NODE.getNode().equals(result.getData().getNode())){
                return "forward:/form/login/"+phone;
            }else if (NodeEnum.SETPAYPWD_NODE.getNode().equals(result.getData().getNode())){
                return "forward:/form/setPassword/"+phone;
            }else if (NodeEnum.BANKCARD_NODE.getNode().equals(result.getData().getNode())){
                return "forward:/form/jumpBankCard/"+phone;
            }else if (NodeEnum.UNSIGN_NODES.getNode().equals(result.getData().getNode())){
                return "forward:/form/jumpSign/"+phone;
            }else if (NodeEnum.DETAILS_NODES.getNode().equals(result.getData().getNode())){
                return "forward:/form/jumpDetails/"+phone+"/"+result.getData().getBorrNum();
            }else {
                throw new CommonException(201,"当前订单状态有误，请重试");
            }
        }else {
            throw new CommonException(201,result.getInfo());
        }

    }

    @ApiOperation(value = "登陆页面跳转", hidden = true)
    @RequestMapping("/login/{phone}")
    public String loginJump(@PathVariable("phone") String phone, Map<String, Object> map) throws CommonException {
        if (!AccountValidatorUtil.isMobile(phone)) {
            throw new CommonException(201, "手机号有误，请返回上一级");
        }
        map.put("phone",phone);
        return "user/login";
    }

    @ApiOperation(value = "设置支付密码/重置密码",notes = "设置支付密码/重置密码",hidden = true)
    @ApiImplicitParam(name = "returnUrl" , value = "跳转返回页面地址，如为null，则按正常流程跳绑卡页面")
    @RequestMapping("/setPassword/{phone}")
    public String setPassword(@PathVariable("phone") String phone,String returnUrl, Map<String,Object> map) {
        //获取用户信息
        Person person = userService.selectByPhone(phone);
        map.put("person",person);
        map.put("returnUrl",returnUrl);
        return "user/setPayPwd";
    }


    @ApiOperation(value = "登陆跳转银行卡认证页面", hidden = true)
    @RequestMapping("/jumpBankCard/{phone}")
    public String jumpBankCard(@PathVariable("phone") String phone,Map<String, Object> map) {
        log.info("用户跳转银行认证界面 phone = "+phone);
        //获取用户信息
        Person person = userService.selectByPhone(phone);
        map.put("person",person);
        return "user/bankCardCertification";
    }

    @ApiOperation(value = "添加银行卡",notes = "添加银行卡",hidden = true)
    @RequestMapping(value = {"/addBankCard/{perId}/{borrId}","/addBankCard/{perId}"})
    public String addBankCard(@PathVariable("perId") String perId,String returnUrl, @PathVariable(required = false) String borrId,Map<String,Object> map) throws CommonException {
        ResponseDo<Person> person = userService.selectPersonById(perId);
        map.put("person",person.getData());
        if (StringUtils.isNotEmpty(borrId)){
            map.put("borrId",borrId);
        }
        map.put("returnUrl",returnUrl);
        return "user/addBankCard";
    }

    @ApiOperation(value = "意见反馈",notes = "用户意见反馈",hidden = true)
    @RequestMapping("/feedback/{perId}")
    public String feedback(@PathVariable("perId") String perId, Map<String,Object> map) {
        map.put("perId",perId);
        return "user/feedback";
    }


    @ApiOperation(value = "忘记支付密码页面")
    @ApiImplicitParam(name = "returnUrl",value = "密码修改之后返回地址")
    @RequestMapping("/forgetPayPwd/{phone}")
    public String forgetPayPwd(@PathVariable("phone") String phone,String returnUrl,Map<String,Object> map){
        map.put("phone",phone);
        map.put("returnUrl",returnUrl);
        return "user/forgetPwd";
    }

    @ApiOperation(value = "银行卡管理")
    @RequestMapping("/bankManagement/{perId}/{phone}/{borrNum}")
    public String bankManagement(@PathVariable("perId") String perId,@PathVariable("phone") String phone,
                                 @PathVariable("borrNum") String borrNum,Map<String,Object> map){
        ResponseDo<List<Bank>> bankManagement = userService.getBankManagement(perId);
        map.put("bankList",bankManagement.getData());
        map.put("perId",perId);
        map.put("phone",phone);
        map.put("borrNum",borrNum);
        return "user/bankManage";
    }
}
