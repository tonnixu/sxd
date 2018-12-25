package com.jhh.dc.loan.constant;

/**
 * 2017/12/27.
 */
public class RedisConstant {

    /**单个产品key*/
    public static final String PRODUCT_KEY= "dc_product";
    /**全部产品key*/
    public static final String PRODUCT_ALL_KEY= "dc_product_all";
    /**每个产品用途key*/
    public static final String PRODUCT_USE_KEY= "dc_product_use_";
    /**全部产品用途key*/
    public static final String PRODUCT_USEALL_KEY= "dc_product_useAll";


    /**注册短信验证码key*/
    public static final String REGISTER_SMS_KEY= "REGISTER_SMS_";
    /**注册短信验证码key*/
    public static final String FORGETPASSWORD_SMS_KEY= "FORGETPASSWORD_SMS_";

    /**短信验证码过期时间*/
    public static final int REGISTER_SMS_TIME= 5*60;

    public static final String VERIFY_COUNT_KEY = "dc_vfy_ct_";
}
