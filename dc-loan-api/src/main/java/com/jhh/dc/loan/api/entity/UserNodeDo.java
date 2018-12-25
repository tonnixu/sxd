package com.jhh.dc.loan.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 2018/7/13.
 */
@Data
public class UserNodeDo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *  用户所在节点
     */
    private String node;

    /**
     *  产品类型 区分随薪贷 随心购等产品地址
     */
    private String prodType;

    /**
     * 合同编号
     */
    private String borrNum;

    /**
     * 产品Id
     */
    private Integer prodId;
}
