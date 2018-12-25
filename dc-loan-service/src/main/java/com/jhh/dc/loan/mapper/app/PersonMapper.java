package com.jhh.dc.loan.mapper.app;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.entity.app.Person;
import com.jhh.dc.loan.entity.app_vo.PersonInfo;
import com.jhh.dc.loan.entity.share.InviterInfo;

public interface PersonMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Person record);

    int insertSelective(Person record);

    Person selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Person record);

    int updateByPrimaryKey(Person record);
    //登录
    List<Person> userLogin(Person person);
    //根据手机号查询，和登录方法查询结果一样
    List<Person> userLoginByPhone(String phone);

    //忘记密码
    int updatePassword(Person person);

    //修改密码
    int personUpdatePassword(Person person);
    
    Person getPersonByPhone(String phone);
    //获取用户当前tokenId
    String getTokenId(String per_id);
    
  //查询个人资料
    Person getPersonInfoByBorr(String brroid);

    PersonInfo getPersonInfo(String perId);
    //检查黑名单
    int checkBlack(String phone);

    int getPersonAvailableBorrowCount(int userId);

    Integer selectByCardNum(String cardNum);
    //查询关注我们的提示信息
    String getFollowUsInfo();

    InviterInfo getInviterInfo(Integer person, Integer persionId);
    //拿取用户和设备信息
    Person getPersonAndDevice(Integer id);

    /**
     * 根据合同号获取身份证号
     * @return
     */
    Map<String,String> getCardNumAndPhoneByBorrId(Integer borrId);

    List getBankByPerId(Integer perId);

    /**清除用户主卡冗余字段*/
    void updatePersonToLoseBank(Integer perId);

}