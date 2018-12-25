package com.jhh.dc.loan.entity.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品表
 */
@Setter
@Getter
@Table(name = "c_product")
@Entity
@ToString
public class Product implements Serializable {

    private static final long serialVersionUID = 5846957251421587101L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String productName;

    private String productDescription;

    private String productIcon;

    private String productTypeCode;

    private String productTypeName;

    private String flag;

    private Float amount;

    private Float penalty;

    private Float forfeitRate;

    private Float interestRate;

    private Float maximumAmount;

    private String remark;

    private String repaymentMethod;

    private String status;

    private Date creationDate;

    private String creationUser;

    private Date updateDate;

    private String updateUser;

    private String sync;

    private Integer contractPrdouctId;

    private Integer termNum;

    private Integer termday;

    private BigDecimal payAmount;

    private BigDecimal serviceFee;

    private Integer serviceFeePosition;

    private Integer companyBody;

    public String getRepaymentMethod() {
        try {
            return RepaymentMethodEunm.valueOf(repaymentMethod).getRepaymentMethod();
        } catch (Exception e) {
            return null;
        }
    }
}
