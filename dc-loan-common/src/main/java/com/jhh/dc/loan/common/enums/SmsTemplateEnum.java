package com.jhh.dc.loan.common.enums;

/**
 * 短信模板整理
 * zhushuaifei
 */
public enum SmsTemplateEnum {
    //随心购
    LOAN_CHECK_CODE_LOGIN(1001,"随心购_验证码登陆",1),
    LOAN_SIGN_SUCCESS(1002,"随心购_签约成功",1),
    LOAN_CARD_SUCCESS(1003,"随心购_放卡成功",1),
    LOAN_REPAY_REMIND(1004,"随心购_还款提醒",1),
    LOAN_REPAY_SUCCESS(1005,"随心购_主动还款成功",1),
    LOAN_DETECT_SUCCESS(1006,"随心购_代扣成功",1),
    LOAN_ALL_REPAY_SUCCESS(1007,"随心购_全部结清",1),
    LOAN_BACK_CHECK_CODE(1008,"随心购_管理后台外网登录验证码",1),
    LOAN_CHECK_CODE_REMIND(1009,"随心购_绑卡验证码发送接口",1),
    LOAN_COMMON_CODE(1010,"随心购_验证码发送接口",1),

    //随心贷
    DC_CHECK_CODE_LOGIN(2001,"随心贷_验证码登陆",2),
    DC_SIGN_SUCCESS(2002,"随心贷_签约成功",2),
    DC_LOAN_SUCCESS(2003,"随心贷_放款成功",2),
    DC_BIND_CARD_CHECK_CODE(2004,"随心贷_绑卡操作验证码",2),
    DC_REPAY_REMIND(2005,"随心贷_还款提醒",2),
    DC_REPAY_SUCCESS_REMIND(2006,"随心贷_主动付款成功",2),
    DC_PAY_SUCCESS_REMIND(2007,"随心贷_代扣成功",2),
    DC_ALL_REPAY_REMIND(2008,"随心贷_全部结清",2),
    DC_BACK_CHECK_CODE(2009,"随心贷_管理后台外网登录验证码",2),
    DC_COMMON_CODE(2010,"随心贷_验证码发送接口",2),



    //悠回收
    LOAN_SUCCESS_REMIND(500001,"放款成功",3),
    RENT_REMIND(500002,"租金提醒",3),
    OVERDUE_REMIND(500003,"逾期提醒",3),
    REPAY_SUCCESS_REMIND(500004,"主动付款成功",3),
    PAY_SUCCESS_REMIND(500005,"代扣成功",3),
    CHECK_CODE_REMIND(500006,"验证码发送接口",3),
    ALL_REPAY_REMIND(500007,"全部还清",3),
    PAY_FAIL_REMIND(500008,"付款失败",3),
    INIT_PASSWORD_REMIND(500009,"初始密码",3),
    COMM_APPROVE_FAIL(500010,"佣金审核未通过",3),
    REFUND_FAIL(500011,"财务退款失败",3),



    //产品类型
    BUY_WITH_HEART(1,"随心购",1),
    LOAN_WITH_HEART(2,"随心贷",2),
    YOU_DUO_DUO(3,"悠多多",3),
    ;


    private Integer code;
    private String desc;
    private int type;  //1:随心购  2:随心贷 3:悠多多

    private SmsTemplateEnum(Integer code, String desc,int type){
        this.code = code;
        this.desc = desc;
        this.type=type;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescByCode(Integer code){
        for (SmsTemplateEnum smsTemplateEnum: SmsTemplateEnum.values()){
            if(smsTemplateEnum.code.equals(code)){
                return smsTemplateEnum.desc;
            }
        }
        return null;
    }

    public static int getTypeByCode(Integer code){
        for (SmsTemplateEnum smsTemplateEnum: SmsTemplateEnum.values()){
            if(smsTemplateEnum.code.equals(code)){
                return smsTemplateEnum.type;
            }
        }
        return 0;
    }
}
