package com.jhh.dc.loan.entity.manager_vo;

import java.io.Serializable;

import com.jhh.dc.loan.entity.app.Bank;

/**
 * 
*描述：
*@author: Wanyan
*@date： 日期：2016年11月10日 时间：下午9:28:15
*@version 1.0
 */

public class BankInfoVo extends Bank implements Serializable {
   private String bank_name;
   private String status_name;
   
/**
 * @return the status_name
 */
public String getStatus_name() {
	return status_name;
}
/**
 * @param status_name the status_name to set
 */
public void setStatus_name(String status_name) {
	this.status_name = status_name;
}
/**
 * @return the bank_name
 */
public String getBank_name() {
	return bank_name;
}
/**
 * @param bank_name the bank_name to set
 */
public void setBank_name(String bank_name) {
	this.bank_name = bank_name;
}
 
}
