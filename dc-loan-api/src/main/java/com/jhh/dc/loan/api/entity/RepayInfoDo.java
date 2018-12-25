package com.jhh.dc.loan.api.entity;

import com.jhh.dc.loan.entity.app.Bank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 2018/7/26.
 */
@Data
public class RepayInfoDo implements Serializable{

    private static final long serialVersionUID = -1L;

    private Integer borrId;

    private Integer perId;

    private Float amountSurplus;

    private List<Bank> bank;

    private Float fee;

    private String phone;

    private String borrNum;

}
