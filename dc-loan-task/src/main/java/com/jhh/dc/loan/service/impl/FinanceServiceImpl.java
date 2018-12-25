package com.jhh.dc.loan.service.impl;

import com.jhh.dc.loan.dao.*;
import com.jhh.dc.loan.entity.loan.PayChannelAdapter;
import com.jhh.dc.loan.entity.manager.CodeValue;
import com.jhh.dc.loan.model.CollectorsRepayData;
import com.jhh.dc.loan.model.FinanceData;
import com.jhh.dc.loan.model.MoneyManagement;
import com.jhh.dc.loan.service.FinanceService;
import com.jhh.dc.loan.service.MailService;
import com.jhh.dc.loan.util.DateUtil;
import com.jhh.dc.loan.util.ExcelUtils;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenchao on 2018/3/29.
 */
@Service
@Log4j
public class FinanceServiceImpl implements FinanceService {

    @Value("${filePath.moneyManagement}")
    private String moneyManagementFilePath;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private MailAddressMapper mailAddressMapper;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private PayChannelAdapterMapper payChannelMapper;

    @Autowired
    private MailService mailService;

    @Value("${system.isTest}")
    private String isTest;

    @Override
    public void sendRepayData() {

        log.info("进入定时发送财务还款数据");
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);// 把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
        String beginDate = DateUtil.getDateString(date);
        List<FinanceData> result =
                borrowListMapper.getFinanceData(beginDate);
        if (result == null || result.size() == 0) {
            result.add(new FinanceData());
        } else {
            Map<String, String> repayTypeMap = getRepayTypeMap();
            result.stream().forEach(t -> {
                t.setType(repayTypeMap.get(t.getTypeWithChannel()));
                t.setSerialNo(Integer.valueOf(t.getTypeNo())>14?t.getSidNo():t.getSerialNo());
            });
        }
        //生成Excel
        String fileName = ExcelUtils.createExcel(result, moneyManagementFilePath, "YHS-HK" + DateUtil.getDateStringyyyymmdd(new Date()));
        //发送邮件
        log.info("发送邮件");
        String[] fileNames = {fileName};
        String[] filePaths = {moneyManagementFilePath + fileName};
        String to = mailAddressMapper.getAddressByType("pay_mail_to");
        String[] toArray = new String[]{};
        if(StringUtils.isNotEmpty(to)){
            toArray = to.split(",");
        }
        String copyto = mailAddressMapper.getAddressByType("pay_mail_copy_to");
        String[] copytoArray = new String[]{};
        if(StringUtils.isNotEmpty(copyto)){
            copytoArray = copyto.split(",");
        }
        mailService.sendMail(toArray, copytoArray, filePaths, fileNames, "随心购/随心贷前一天还款数据", "随心购/随心贷前一天还款数据，请参见附件！");
        new File(moneyManagementFilePath + fileName).delete();
    }

    @Override
    public void sendPayData() {
        Calendar calendar = new GregorianCalendar();
        // 把日期往后增加一天.整数往后推,负数往前移动
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        String beginDate = DateUtil.getDateString(calendar.getTime());
        String endDate = DateUtil.getDateString(new Date());

        Map map = new HashMap();
        map.put("beginDate", beginDate);
        map.put("endDate", endDate);

        List<MoneyManagement> result = borrowListMapper.sendMoneyManagement(map);
        if (result == null || result.size() == 0) {
            result.add(new MoneyManagement());
        } else {
            Map<String, String> payTypeMap = getPayTypeMap();
            //result.stream().forEach(t -> t.setType(payTypeMap.get(t.getTypeWithChannel())));
        }

        //生成Excel
        String fileName = ExcelUtils.createExcel(result, moneyManagementFilePath, "dc_ZJD" + DateUtil.getDateStringyyyymmdd(new Date()));

        //发送邮件
        log.info("发送邮件");
        String to = mailAddressMapper.getAddressByType("repay_mail_to");
        String[] toArray = new String[]{};
        if(StringUtils.isNotEmpty(to)){
            toArray = to.split(",");
        }
        String copyto = mailAddressMapper.getAddressByType("repay_mail_copy_to");
        String[] copytoArray = new String[]{};
        if(StringUtils.isNotEmpty(copyto)){
            copytoArray = copyto.split(",");
        }
        mailService.sendMail(toArray, copytoArray, moneyManagementFilePath + fileName, fileName, "【随心购/随心贷】_放款数据", "【随心购/随心贷】_放款数据");
    }

    private Map<String, String> getRepayTypeMap() {

        List<CodeValue> orderTypes = codeValueMapper.getCodeValueListByCode("order_type");
        List<CodeValue> payCenterChannels = codeValueMapper.getCodeValueListByCode("pay_center_channel");

        Map<String, String> repayTypeMap = new HashMap<>();
        repayTypeMap.put("6", "减免");
        repayTypeMap.put("7", "线下还款");

        /*repayTypeMap.put("1", "放款(银生宝)");
        repayTypeMap.put("5", "主动还款(银生宝)");
        repayTypeMap.put("4", "还款(代收)(银生宝)");
        repayTypeMap.put("8", "批量代扣(银生宝)");
        repayTypeMap.put("11", "放款(海尔)");
        repayTypeMap.put("12", "主动还款(海尔)");
        repayTypeMap.put("13", "还款(代收)(海尔)");
        repayTypeMap.put("14", "批量代扣(海尔)");*/


        List<PayChannelAdapter> channels = payChannelMapper.selectAll();
        for (int i = 0; i < channels.size(); i++) {
            PayChannelAdapter adapter = channels.get(i);
            if ("15".equals(adapter.getType())) {
                repayTypeMap.put(adapter.getType() + "/" + adapter.getChannel(), "放款(" + adapter.getName() + ")");
            }
            if ("16".equals(adapter.getType())) {
                repayTypeMap.put(adapter.getType() + "/" + adapter.getChannel(), "主动还款(" + adapter.getName() + ")");
            }
            if ("17".equals(adapter.getType())) {
                repayTypeMap.put(adapter.getType() + "/" + adapter.getChannel(), "还款(代收)(" + adapter.getName() + ")");
            }
            if ("18".equals(adapter.getType())) {
                repayTypeMap.put(adapter.getType() + "/" + adapter.getChannel(), "批量代扣(" + adapter.getName() + ")");
            }
        }

        for (int j = 0; j < orderTypes.size(); j++) {
            CodeValue orderType = orderTypes.get(j);
            Integer orderTypeValue = Integer.valueOf(orderType.getCodeCode());
            if (orderTypeValue < 15 || orderTypeValue > 18) {
                continue;
            }

            if (!repayTypeMap.containsKey(orderType.getCodeCode())) {
                repayTypeMap.put(orderType.getCodeCode(), orderType.getMeaning() + "(支付中心)");
            }
        }
        return repayTypeMap;
    }

    private Map<String, String> getPayTypeMap() {
        List<CodeValue> payCenterChannels = codeValueMapper.getCodeValueListByCode("pay_center_channel");
        Map<String, String> payTypeMap = new HashMap<>();
        payTypeMap.put("1", "银生宝");
        payTypeMap.put("11", "海尔金融");
        payTypeMap.put("99", "佣金提现");

        for (int i = 0; i < payCenterChannels.size(); i++) {
            CodeValue value = payCenterChannels.get(i);
            payTypeMap.put("15/" + value.getCodeCode(), value.getMeaning());
        }
        return payTypeMap;
    }

}
