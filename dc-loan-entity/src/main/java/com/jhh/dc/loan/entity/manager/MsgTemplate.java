package com.jhh.dc.loan.entity.manager;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity @Table(name = "c_msg_template") @Getter @Setter
public class MsgTemplate implements Serializable{
    private Integer id;

    private String title;

    private String employNum;

    private String status;

    private Date updateDate;

    private Date createTime;

    private String sync;

    private String content;
}