package com.jhh.dc.loan.entity.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter @ToString
public class BankVo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer id;
    private Integer bankId;
    private String bankNum;
    private String bankName;
    private String bankCode;
    
    private Integer status;
    private String phone;
    private Date createDate;

}
