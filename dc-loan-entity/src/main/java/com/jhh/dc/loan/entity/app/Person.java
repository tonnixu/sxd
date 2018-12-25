package com.jhh.dc.loan.entity.app;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="b_person")
@Getter
@Setter
@ToString
@ApiModel("person")
public class Person implements Serializable {
    private static final long serialVersionUID = 734164289531433453L;
    @Id
    private Integer id;
    private String phone;
    private Date createDate;
    private Date updateDate;
    private String sync;
    private String isManual;
    private String description;
    private String cardNum;
    private String name;
    private String bankName;
    private String bankCard;
    private Integer contactNum;
    @Transient
    private BigDecimal balance;
    private Date loginTime;
    private Integer isLogin;
    private String tokenId;

    @Transient
    private String source;
    @Transient
    private String contactUrl;
    @Transient
    private String device;

    private String payPassword;


}