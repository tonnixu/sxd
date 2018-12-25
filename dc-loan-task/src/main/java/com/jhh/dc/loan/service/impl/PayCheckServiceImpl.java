package com.jhh.dc.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.jhh.dc.loan.dao.BorrowListMapper;
import com.jhh.dc.loan.dao.CodeValueMapper;
import com.jhh.dc.loan.dao.LoanOrderDOMapper;
import com.jhh.dc.loan.dao.PayChannelAdapterMapper;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.loan.PayChannelAdapter;
import com.jhh.dc.loan.entity.manager.CodeValue;
import com.jhh.dc.loan.model.FinanceData;
import com.jhh.dc.loan.model.LoanOrderDO;
import com.jhh.dc.loan.model.MoneyManagement;
import com.jhh.dc.loan.service.FinanceService;
import com.jhh.dc.loan.service.MailService;
import com.jhh.dc.loan.service.PayCheckService;
import com.jhh.dc.loan.util.DateUtil;
import com.jhh.dc.loan.util.ExcelUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * Created by chenchao on 2018/3/29.
 */
@Service
@Log4j
public class PayCheckServiceImpl implements PayCheckService {

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private LoanOrderDOMapper loanOrderDoMapper;

    @Autowired
    private MailService mailService;

    @Value("${system.isTest}")
    private String isTest;

    @Override
    public void payCheck() {
        String[] to = {"chenzhen@jinhuhang.com.cn", "chenchao@jinhuhang.com.cn", "yezhuangqiao@jinhuhang.com.cn", "liuhongming@jinhuhang.com.cn", "dinghaifeng@jinhuhang.com.cn"};
        if ("on".equals(isTest)) {
            to = new String[]{"chenchao@jinhuhang.com.cn","wangzhixing@jinhuhang.com.cn"};
        }

        String multiPayByPersonSql = "SELECT\n" +
                "\tt.per_id as '用户ID', t1.name as '姓名', t1.phone as '手机号', count(1) as '放款次数'\n" +
                "FROM\n" +
                "\tb_borrow_list t\n" +
                "Left JOIN b_person t1\n" +
                "on t.per_id = t1.id\n" +
                "WHERE\n" +
                "\tt.per_id IN (\n" +
                "\t\tSELECT\n" +
                "\t\t\tper_id\n" +
                "\t\tFROM\n" +
                "\t\t\tb_borrow_list t\n" +
                "\t\tWHERE\n" +
                "\t\t\tt.pay_date > CURDATE()\n" +
                "\t\tAND t.borr_status IN ('BS004', 'BS005')\n" +
                "\t)\n" +
                "AND t.borr_status IN ('BS004', 'BS005')\n" +
                "GROUP BY\n" +
                "\tper_id\n" +
                "HAVING\n" +
                "\tCOUNT(1) > 1";
        List<LinkedHashMap> multiplePayByPersonList = (List<LinkedHashMap>) borrowListMapper.getMultiplePayByPerson();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < multiplePayByPersonList.size(); i++) {
            HashMap map = multiplePayByPersonList.get(i);
            buffer.append(JSON.toJSONString(map)).append("\n");
        }

        if (buffer.toString().trim().length() > 0) {
            //发送邮件
            log.info("发送随心贷单人多笔合同重复放款邮件");
            String content = ("查询数据：\n" + buffer.toString().trim() + "\n\n查询SQL:\n" + multiPayByPersonSql);
            content = content.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;");
            mailService.sendMail(to, "随心贷单人多笔合同重复放款", content);
        }

        List<LinkedHashMap> multiplePayByBorrowList = (List<LinkedHashMap>) loanOrderDoMapper.getMultiplePayByBorrowList();

        String multiplePayByBorrowListSql = "SELECT\n" +
                "\tt.borr_num as '合同单号', t1.name as '姓名', t1.phone as '手机号', count(1) as '放款次数'\n" +
                "FROM\n" +
                "\tb_loan_order t\n" +
                "Left JOIN b_person t1\n" +
                "on t.per_id = t1.id\n" +
                "WHERE\n" +
                "\tt.borr_num IN (\n" +
                "\t\tSELECT\n" +
                "\t\t\tt.borr_num\n" +
                "\t\tFROM\n" +
                "\t\t\tb_loan_order t\n" +
                "\t\tWHERE\n" +
                "\t\t\tt.creation_date > CURDATE()\n" +
                "\t\tAND t.rl_state = 's' and t.type = '15'\n" +
                "\t)\n" +
                "AND t.rl_state = 's' and t.type = '15'\n" +
                "GROUP BY\n" +
                "\tborr_num\n" +
                "HAVING\n" +
                "\tCOUNT(1) > 1";

        buffer = new StringBuilder();
        for (int i = 0; i < multiplePayByBorrowList.size(); i++) {
            HashMap map = multiplePayByBorrowList.get(i);
            buffer.append(JSON.toJSONString(map)).append("\n");
        }

        if (buffer.toString().trim().length() > 0) {
            //发送邮件
            log.info("发送随心贷单笔合同重复放款邮件");
            String content = ("查询数据：\n" + buffer.toString().trim() + "\n\n查询SQL:\n" + multiplePayByBorrowListSql);
            content = content.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;");
            mailService.sendMail(to, "随心贷单笔合同重复放款", content);
        }
    }

}
