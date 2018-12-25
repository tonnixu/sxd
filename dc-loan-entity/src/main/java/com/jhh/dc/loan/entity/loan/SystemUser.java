package com.jhh.dc.loan.entity.loan;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "c_system_user")
@Getter
@Setter
public class SystemUser implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String guid;
    private String userSysno;
    private String userName;
    private Integer level;
    private Integer productSysno;
    private Integer bedueLevel;
    private Integer userGroupId;
    private String status;
    private String createUser;
    private Date createDate;
    private String updateUser;
    private Date updateDate;
    private Integer levelType;
    private Integer isManage;
    private String password;
    private String phone;
    private Double channelPercent;


    @Transient
    private int saveType;

    public SystemUser(){}

    public SystemUser(String userSysno, String userName, Integer userGroupId, String status, Integer levelType, Integer isManage, String password, String phone) {
        this.userSysno = userSysno;
        this.userName = userName;
        this.userGroupId = userGroupId;
        this.status = status;
        this.levelType = levelType;
        this.isManage = isManage;
        this.password = password;
        this.phone = phone;
    }



    @Override
    public String toString() {
        return "CollectorsLevel{" +
                "userSysno='" + userSysno + '\'' +
                ", userName='" + userName + '\'' +
                ", userGroupId=" + userGroupId +
                ", status='" + status + '\'' +
                ", levelType=" + levelType +
                ", isManage=" + isManage +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
