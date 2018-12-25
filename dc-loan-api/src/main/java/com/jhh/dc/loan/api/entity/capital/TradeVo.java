package com.jhh.dc.loan.api.entity.capital;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 2018/4/13.
 */
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TradeVo implements Serializable {
    private static final long serialVersionUID = -5186971048123922579L;

    public TradeVo(int perId, String serialNo, String payChannel, Integer triggerStyle, String bankNum, Float amount,Integer appId) {
        this.perId = perId;
        this.serialNo = serialNo;
        this.payChannel = payChannel;
        this.triggerStyle = triggerStyle;
        this.bankNum = bankNum;
        this.amount = amount;
        this.appId = appId;
    }

    private int perId;

    /**
     * 订单编号
     */
    private String serialNo;

    /**
     * 渠道编号
     */
    private String payChannel;

    /**
     * 触发条件
     */
    private Integer triggerStyle;

    private String bankNum;

    /**
     * 金额
     */
    private Float amount;

    /**
     * 验证码
     */
    private String validateCode;

    /**
     * 验证码渠道
     */
    private String msgChannel;

    /**
     * 支付类型
     */
    private Integer payType;

    /**
     * 对应appid
     */
    private Integer appId;

    /**
     *  公司主体
     */
    private Integer companyBody;

    private String name;

    private String phone;

    private String bankName;

    private String bankCode;

    private String cardNum;
}
