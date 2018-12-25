package com.jhh.dc.loan.api.entity;

import com.jhh.dc.loan.common.util.CodeReturn;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.jhh.dc.loan.common.util.CodeReturn.*;

/**
 * 2018/7/17.
 */
@Setter
@Getter
public class DetailsDo implements Serializable{

    private static final long serialVersionUID = 1L;

    private Integer perId;

    private Integer borrId;

    private Date askborrDate;

    private String borrNum;

    private float borrAmount;

    private Integer termNum;

    private Date payDate;

    private Date planrepayDate;

    private Date actRepayDate;

    private float planRepay;

    private String borrStatus;

    private String borrStatusName;

    private int termDay;

    private float amountSurplus;

    private String prodType;

    private Integer prodId;

    public void setBorrName(String borrStatus, String prodType) {
        if (CodeReturn.STATUS_PAY_BACK.equals(borrStatus)){
            this.borrStatusName = "已还清";
        }
        if (CodeReturn.STATUS_DELAY_PAYBACK.equals(borrStatus)){
            this.borrStatusName = "逾期还清";
        }
        if (CodeReturn.STATUS_EARLY_PAYBACK.equals(borrStatus)){
            this.borrStatusName = "提前结清";
        }
        if (PRODUCT_TYPE_CODE_CARD.equals(prodType)) {
            if (STATUS_TO_REPAY.equals(borrStatus)) {
                this.borrStatusName = "待付款";
            } else if (STATUS_LATE_REPAY.equals(borrStatus)) {
                this.borrStatusName = "逾期未付";
            } else if (STATUS_COM_PAYING.equals(borrStatus)) {
                this.borrStatusName = "发放中";
            } else if (STATUS_COM_PAY_FAIL.equals(borrStatus)) {
                this.borrStatusName = "发放中";
            }
        }else if (PRODUCT_TYPE_CODE_MONEY.equals(prodType)){
            if (STATUS_TO_REPAY.equals(borrStatus)) {
                this.borrStatusName = "待还款";
            } else if (STATUS_LATE_REPAY.equals(borrStatus)) {
                this.borrStatusName = "逾期未还";
            } else if (STATUS_COM_PAYING.equals(borrStatus)) {
                this.borrStatusName = "放款中";
            } else if (STATUS_COM_PAY_FAIL.equals(borrStatus)) {
                this.borrStatusName = "放款中";
            }
        }
    }

}
