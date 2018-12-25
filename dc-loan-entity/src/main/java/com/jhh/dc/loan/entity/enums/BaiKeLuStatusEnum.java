package com.jhh.dc.loan.entity.enums;

import org.apache.commons.lang.StringUtils;

/**
 * Create by carl.wan on 2018/1/4
 */
public enum BaiKeLuStatusEnum {
    NO_SEND("0","未拨打"),
    SEND_ING("1","拨打中"),
    UN_DONE("2","未完成"),
    PASS("3","通过"),
    UN_PASS("4","拒绝"),
    BUSY("5","未接通"),
    NOT_ONESELF("6","非本人"),
    BUSY_SIGNAL("7","忙音"),
    NO_ANSWER("8","未接"),
    SHUTDOWN("9","关机"),
    HALT("10","停机"),
    EMPTY("11","空号"),
    SHIELDING("12","停机"),
    REFUSED("13","拒绝"),
    CONNECTED("14","无法接通"),
    UNKNOWN("9999","未知");

    BaiKeLuStatusEnum(String code, String describe){
        this.code = code;
        this.describe = describe;
    }

    public static String getDescByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        for (BaiKeLuStatusEnum statusEnum : BaiKeLuStatusEnum.values()) {
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
