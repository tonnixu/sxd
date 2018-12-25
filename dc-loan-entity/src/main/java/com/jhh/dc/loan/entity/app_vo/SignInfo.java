package com.jhh.dc.loan.entity.app_vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by xuepengfei on 2018/1/19.
 */
@Getter
@Setter
public class SignInfo implements Serializable {

    private static final long serialVersionUID = 734164289531433453L;
    private int perId ;
    private int borrId;
    private String name;
    private Float borrAmount;
    private Integer termNum;
    private Integer termDay;
    private String bankNum;
    private Float planRepay;
    private BigDecimal payAmount;
    private BigDecimal interestRate;
    private BigDecimal serviceFee;
    /**
     *  服务费缴费方式
     */
    private Integer serviceFeePosition;
    /**
     *  温馨提示
     */
    private String signHint;



}
