package com.jhh.dc.loan.entity.manager_vo;

import java.io.Serializable;

import com.jhh.dc.loan.entity.manager.Feedback;

public class FeedbackVo extends Feedback implements Serializable{
	private String phone;
	private String productName;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
  
}