package com.jhh.dc.loan.util;

import com.jhh.dc.loan.common.util.CodeReturn;
import com.jhh.dc.loan.entity.app.BorrowList;

import java.util.ArrayList;
import java.util.List;

import static com.jhh.dc.loan.common.util.CodeReturn.*;

/**
 * 2018/11/14.
 */
public class BorrowUtil {

    public static String setBorrName(String borrStatus, String prodType) {
        if (CodeReturn.STATUS_PAY_BACK.equals(borrStatus)){
            return "已还清";
        }
        if (CodeReturn.STATUS_DELAY_PAYBACK.equals(borrStatus)){
            return "逾期还清";
        }
        if (CodeReturn.STATUS_EARLY_PAYBACK.equals(borrStatus)){
            return "提前结清";
        }
        if (PRODUCT_TYPE_CODE_CARD.equals(prodType)) {
            if (STATUS_TO_REPAY.equals(borrStatus)) {
                return "待付款";
            } else if (STATUS_LATE_REPAY.equals(borrStatus)) {
                return "逾期未付";
            } else if (STATUS_COM_PAYING.equals(borrStatus)) {
                return "发放中";
            } else if (STATUS_COM_PAY_FAIL.equals(borrStatus)) {
                return "发放失败";
            }
        } else if (PRODUCT_TYPE_CODE_MONEY.equals(prodType)) {
            if (STATUS_TO_REPAY.equals(borrStatus)) {
                return "待还款";
            } else if (STATUS_LATE_REPAY.equals(borrStatus)) {
                return "逾期未还";
            } else if (STATUS_COM_PAYING.equals(borrStatus)) {
                return "放款中";
            } else if (STATUS_COM_PAY_FAIL.equals(borrStatus)) {
                return "放款失败";
            }
        }
        return null;
    }

    /**
     *  判断合同是否为最终状态
     * @param borrowList
     * @return
     */
    public static boolean borrowFinalState(BorrowList borrowList){
        if (borrowList != null) {
            List<String> status = new ArrayList<>();
            status.add(STATUS_CANCEL);
            status.add(STATUS_PAY_BACK);
            status.add(STATUS_REVIEW_FAIL);
            status.add(STATUS_PHONE_REVIEW_FAIL);
            status.add(STATUS_DELAY_PAYBACK);
            status.add(STATUS_EARLY_PAYBACK);
            return status.contains(borrowList.getBorrStatus());
        }
        return true;
    }
}
