package com.jhh.dc.loan.app.web.logic;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.dc.loan.api.app.LoanService;
import com.jhh.dc.loan.api.app.RiskService;
import com.jhh.dc.loan.api.app.UserService;
import com.jhh.dc.loan.api.entity.ForgetPayPwdVo;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.sms.SmsService;
import com.jhh.dc.loan.api.entity.LoginVo;
import com.jhh.dc.loan.app.web.exception.ExceptionJsonController;
import com.jhh.dc.loan.entity.app.Person;
import com.jhh.dc.loan.entity.app_vo.BorrowListVO;
import com.jhh.dc.loan.entity.app_vo.JdCardDetailVO;
import com.jhh.dc.loan.entity.app_vo.JdCardInfoVO;
import com.jhh.dc.loan.entity.app_vo.JdCardKeyInfo;
import com.jhh.dc.loan.entity.manager.Feedback;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户模块
 *
 * @author
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends ExceptionJsonController {

    @Reference
    private UserService userService;

    @Reference
    private RiskService riskService;

    @Reference
    private SmsService smsService;
    @Reference
    private LoanService loanService;


    @ApiOperation(value = "用户信息修改", notes = "同步A公司用户信息")
    @ApiImplicitParam(name = "person", value = "用户实体person", required = true)
    @PostMapping("/updatePersonInfo")
    public ResponseDo<?> userUpdateRegister(Person person) {
        return userService.updatePersonInfo(person);

    }


    @ApiOperation(value = "用户登陆", notes = "B公司用户登录" ,hidden = true)
    @RequestMapping("/login")
    public ResponseDo<Person> login(LoginVo vo) {
        return userService.login(vo);

    }

    @ApiOperation(value = "保存或修改用户支付密码")
    @RequestMapping("/setPassword")
    public ResponseDo<?> setPassword(@RequestParam String phone,@RequestParam String paypwd,@RequestParam String confirmPaypwd){
        return userService.userSetPassword(phone,paypwd,confirmPaypwd);
    }

    @ApiOperation(value = "A公司获取我的卡包信息")
    @GetMapping("/getMyCardsByPhone/{phone}")
    public List<JdCardInfoVO> getMyCardsByPhone(@ApiParam(value = "用户手机号", required = true) @PathVariable("phone") String phone){
        return userService.getMyCardsByPhone(phone);
    }

    @ApiOperation(value = "A公司获取我的账单信息")
    @GetMapping("/getBorrowListByPhone/{phone}")
    public List<BorrowListVO> getBorrowListByPhone(@ApiParam(value = "用户手机号", required = true) @PathVariable("phone") String phone){
        return userService.getBorrowListByPhone(phone);
    }

    @ApiOperation(value = "B公司H5更新京东卡表fetch字段同时获取卡密信息")
    @PostMapping("/updateFetchAndGetCardNum/{jdCardId}")
    public ResponseDo<JdCardKeyInfo> updateFetchAndGetCardNum(@ApiParam(value = "京东卡id", required = true) @PathVariable("jdCardId") Integer jdCardId,
                                                  @ApiParam(value = "用户手机号", required = true) @RequestParam("phone") String phone,
                                                  @ApiParam(value = "密码", required = true) @RequestParam("password") String password){
        ResponseDo<JdCardKeyInfo> responseDo;
        try {
            responseDo = userService.updateFetchAndGetCardNum(jdCardId,phone,password);
        } catch (Exception e) {
            responseDo = ResponseDo.newFailedDo("获取卡密信息失败");
            log.error("获取卡密信息失败:"+ExceptionUtils.getRootCauseMessage(e));
        }
        return responseDo;
    }

    @ApiOperation(value = "意见反馈")
    @RequestMapping("/feedback")
    public ResponseDo<?> addFeedback(Feedback feedback){
        return userService.userFeedBack(feedback);
    }

    @ApiOperation(value = "忘记密码验证")
    @RequestMapping("/forgetPayPwd")
    public ResponseDo<?> forgetPayPwd(ForgetPayPwdVo vo){
        return userService.forgetPayPwd(vo);
    }



    @ApiOperation(value = "A公司获取我的账单信息")
    @GetMapping("/getBorrowListByPersonId/{personId}")
    public List<BorrowListVO> getBorrowListByPersonId(@ApiParam(value = "用户id", required = true) @PathVariable("personId") Integer personId){
        return userService.getBorrowListByPersonId(personId);
    }
}