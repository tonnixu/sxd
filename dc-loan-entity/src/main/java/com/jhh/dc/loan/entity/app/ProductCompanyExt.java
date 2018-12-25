package com.jhh.dc.loan.entity.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProductCompanyExt implements Serializable{

    private static final long serialVersionUID = 2510888469306589931L;

    private Integer companyBodyId;

    private String property;

    private String value;

    private Integer status;

    private String sync;

}