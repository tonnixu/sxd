package com.jhh.dc.loan.entity.enums;

import org.apache.commons.lang.StringUtils;

/**
 * 2018/10/10.
 */
public enum WhiteListEnum {


    KN_WHITE_LIST("1","快牛白名单");

    WhiteListEnum(String code,String describe){
        this.code = code;
        this.describe = describe;
    }

    public static String getDescByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        for (WhiteListEnum statusEnum : WhiteListEnum.values()) {
            if(StringUtils.equals(statusEnum.code,code)){
                return statusEnum.describe;
            }
        }
        return null;
    }


    private String code;

    private String describe;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
