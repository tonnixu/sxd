package com.jhh.dc.loan.api.app;

import com.jhh.dc.loan.api.entity.ForgetPayPwdVo;
import com.jhh.dc.loan.api.entity.LoginVo;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.entity.app.Bank;
import com.jhh.dc.loan.entity.app.NoteResult;
import com.jhh.dc.loan.entity.app.Person;
import com.jhh.dc.loan.entity.app_vo.BorrowListVO;
import com.jhh.dc.loan.entity.app_vo.JdCardInfoVO;
import com.jhh.dc.loan.entity.app_vo.JdCardKeyInfo;
import com.jhh.dc.loan.entity.manager.Feedback;

import java.util.List;

public interface UserService {



	ResponseDo<Person> selectPersonById(String userId);

	ResponseDo<?> userFeedBack(Feedback feed);


	String setMessage(String userId, String templateId, String params);

    String setRedisData(String key,int time,String data);

    String queryRedisData(String key);

	/**
	 * 查询快捷支付绑定状态并发送验证码
	 * @param phone
	 * @param bankNum
	 * @param perId
     * @return
	 */
	NoteResult queryBindAndSendMsg(String phone, String bankNum, String perId);

	Person selectByPhone(String phone);

	ResponseDo<?> updatePersonInfo(Person person);

	ResponseDo<Person> login(LoginVo vo);

	List<JdCardInfoVO> getMyCardsByPhone(String phone);

	ResponseDo<JdCardKeyInfo> updateFetchAndGetCardNum(Integer jdCardId, String phone, String password) throws Exception;

	List<BorrowListVO> getBorrowListByPhone(String phone);

	/**
	 *  设置密码
	 * @param phone
     * @return
     */
    ResponseDo<?> userSetPassword(String phone, String paypwd, String confirmPaypwd);

	/**
	 *  忘记密码
	 * @param vo
	 * @return
     */
	ResponseDo<?> forgetPayPwd(ForgetPayPwdVo vo);

    List<BorrowListVO> getBorrowListByPersonId(Integer personId);

	/**
	 *  获取用户银行卡
	 * @param perId
	 */
	ResponseDo<List<Bank>> getBankManagement(String perId);
}