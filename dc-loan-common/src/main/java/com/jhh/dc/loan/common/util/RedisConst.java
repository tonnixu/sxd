package com.jhh.dc.loan.common.util;

/**
 * redis 中的key常量
 * Created by xuepengfei on 2017/7/20.
 */
public class RedisConst {
    public static final String SEPARATOR = ":";

    public static final String BP_KEY = "dc_a_bp_";
    public static final String PR_KEY = "dc_a_pr_t_";
    public static final String CITY_KEY = "dc_a_app_get_city";
    public static final String REVIEW_KEY = "dc_a_sng_rew_";
    public static final String ORDER_KEY = "dc_a_SCZ_oid_";
    public static final String ORDER_REFUND_KEY = "dc_a_SCZ_refund_";
    public static final String REPAYMENT_KEY = "dc_a_SCZ_rnt_";
    public static final String PAYCONT_KEY = "dc_a_PC_bid_";
    public static final String REGISTER_KEY = "dc_a_UR_";
    public static final String DOWNLOAD_KEY = "dc_a_app_dad_aod";
    public static final String PRODUCT_KEY = "dc_a_app_pct_info";
    public static final String QUESTION_KEY = "dc_a_con_qns";
    public static final String NODE_KEY = "dc_a_nds_";
    public static final String CONTACTS_NUM_KEY = "dc_a_cont_num_";
    public static final String PAY_ORDER_KEY = "dc_a_bc_tag_";
    public static final String PAY_REFUND_KEY = "dc_a_refund_tag_";
    public static final String JUXINLI_TOKEN = "dc_a_jxl_tk_";
    public static final String CONTACT_LIMIT = "dc_a_contact_lmt";
    public static final String NOT_EXIST_ORDERS = "dc_a_not_exist_orders";
    public static final String AMOUNT_LIMIT = "dc_a_amount_limit";
    public static final String DELAY_QUEUE = "dc_a_delay_queue";
    public static final String QUEUE_ELEMENT_EXIST = "dc_a_q_e_exist_";
    public static final String ORDER_QUEUE_NAME = "dc_a_order_queue";
    public static final String SHARE_RULES = "dc_a_share_rules";
    public static final String SHARE_PATH = "dc_a_share_url";
    public static final String SHARE_PATH_RULES = "dc_a_share_url_rules"; // 分享规则流程图片
    public static final String SHARE_DATA = "dc_a_share_data";
    public static final String COMMISSION_MINIMUM_AMOUNT = "dc_a_commission_min";

    public static final String AGENT_PAY_QUERY_YSB = "AGENT_PAY_QUERY_YSB_";
    public static final String AGENT_PAY_QUERY_JHH_YSB = "AGENT_PAY_QUERY_JHH_YSB";
    // 清结算锁
    public static final String SETTLE_LOCK_KEY = "kk:st:power:switch";

    public static final String DRAINAGE_STAT = "dc_DRAINAGE_STAT";


    //当前认证流程
    public static final String BPM_LIST_KEY = "dc_a_bpm_list";

    public static final String PAY_TYPE_KEY = "dc_a_payType";

    public static final String DEDUCT_TYPE_KEY = "dc_a_deductType";
    // 退款redis支付渠道key
    public static final String REFUND_TYPE_KEY = "dc_a_refundType";

    //还款流水时间限制key
    public static final String ORDER_HISTORY_QUERY_TIME = "dc_order_his_query_time";
    //还款流水单
    public static final String ORDER_HISTORY_KEY = "dc_order_his";

    //支付渠道_拉卡拉
    public static final String PAY_CHANNEL_LKL = "dc_pay_channel_lkl";

    //提现操作锁
    public static final String COMMISSION_WITHDRAWAL_LOCK = "dc_COMMISSION_WITHDRAW_";

    // 用户验证码
    public static final String VALIDATE_CODE = "dc_VALIDATE_CODE";

    // 用户获取验证码状态
    public static final String VALIDATE_CODE_STATUS = "dc_VALIDATE_CODE_STATUS";

    // 快捷支付验证码渠道
    public static final String VALIDATE_CODE_CHANNEL = "dc_VALIDATE_CODE_CHANNEL";

    // 合利宝获取短信全局自增订单号
    public static final String HELI_PAY_MSG_ORDER_NUM = "dc_HELI_PAY_MSG_ORDER_NUM";

    public static final String BATCHDEDUCT_LOCK= "dc_batchDeduct_click_sleep";

    //注册提醒通知
    public static final String REGISTER_REMIND_NOTICE  = "dc_register_notice";
    //登录提醒通知
    public static final String LOGIN_REMIND_NOTICE = "dc_login_notice";
    //注册提醒通知锁
    public static final String REGISTER_REMIND_NOTICE_LOCK  = "dc_register_notice_lock";
    //登录提醒通知锁
    public static final String LOGIN_REMIND_NOTICE_LOCK = "dc_login_notice_lock";


    //百可录是否打电话总开关
    public static final String BAIKELU_ALL_IS_OPEN_KEY="dc_baikelu_all_onOff";
    // app打开注册的开关
    public static final String APP_SUBSCRIBE_FLAG = "dc_app_subscribe_flag";

    /**白骑士tokenkey*/
    public static final String APP_USER_BQSTONKEY = "dc_app_tokenKey_";

    public static final String APP_USER_FEEDBACK = "dc_app_feedback_";
    //用户登陆是存入token 类似session过期时间
    public static final String APP_USER_TOKEN = "dc_app_token_";

    //风控认证节点产品Id
    public static final String DC_LOAN_RISK_NODE_KEY="dc_loan_risk_node_key";
    // 风控认证白名单产品Id
    public static final String DC_LOAN_RISK_NODE_WHITE_KEY = "white_directory_productId";

    //限制Ip访问次数key
    public static final String DC_LOAN_IP_LIMIT_KEY="dc_loan_ip:";
    //用户每日申请限制KEY
    public static final String DC_LOAN_APPLY_LIMIT_KEY="dc_loan:apply:limit";
    //用户每日申请限制KEY 最大次数
    public static final String DC_LOAN_APPLY_MAX_NUMBER_KEY="dc_loan:max:number";


}
