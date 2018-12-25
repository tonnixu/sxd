package com.jhh.dc.loan.entity.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class BorrPerInfo implements Serializable {
	
	private String borrId;
	private String name;
	private double maximumAmount;
	private int termValue;
	private String planrepayDate;
	private double monthlyRate;
	private String phone;
	private String surplusQuota;
	private String surplusPenalty;
	private String surplusPenaltyInteres;
	private String prodType;
	private String totalTermNum;
	

	
	
}
