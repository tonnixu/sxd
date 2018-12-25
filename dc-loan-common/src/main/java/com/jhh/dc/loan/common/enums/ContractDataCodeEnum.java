package com.jhh.dc.loan.common.enums;

/**
 * 电子合同Data数据类型枚举
 * zhushuaifei
 */
public enum ContractDataCodeEnum {
    GO_TO_BUY("1","pay_card"),   //随心购
    AS_CREDIT("2","pay_money");  //随心贷


    private String code;
    private String desc;

    private ContractDataCodeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getDescByCode(String code){
        for (ContractDataCodeEnum payChannelEnum: ContractDataCodeEnum.values()){
            if(payChannelEnum.code.equals(code)){
                return payChannelEnum.desc;
            }
        }
        return null;
    }
}
