package com.jhh.dc.loan.service.app;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.jhh.dc.loan.api.constant.Constants;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.entity.app.Bank;
import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.app.Person;
import com.jhh.dc.loan.entity.app.Product;
import com.jhh.dc.loan.mapper.app.BankMapper;
import com.jhh.dc.loan.mapper.app.BorrowListMapper;
import com.jhh.dc.loan.mapper.app.PersonMapper;
import com.jhh.dc.loan.mapper.product.ProductCompanyExtMapper;
import com.jhh.dc.loan.mapper.product.ProductMapper;
import com.jhh.dc.loan.util.BorrowUtil;
import com.jhh.pay.driver.pojo.BindChannelsRequest;
import com.jhh.pay.driver.pojo.QueryBindChannelResp;
import com.jhh.pay.driver.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 2018/1/2.
 */
@Service
@Slf4j
public class VerifyServiceImpl {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private ProductCompanyExtMapper productCompanyExtMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private TradeService tradeService;

    /**
     * 验证用户是否存在
     *
     * @param perId
     * @return
     */
    public boolean verifyPerson(String perId) {

        Person p = personMapper.selectByPrimaryKey(Integer.valueOf(perId));
        return p != null;
    }

    /**
     * 根据银行卡主体删除是否删除银行卡
     *
     * @param
     */
    @Transactional
    void verifyBankBody(String perId) {
        Bank bank = bankMapper.selectPrimayCardByPerId(perId);
        BorrowList nowBorr = borrowListMapper.selectNow(Integer.parseInt(perId));
        //判断当前合同是新公司还是老公司
        Product product = productMapper.selectByPrimaryKey(nowBorr.getProdId());
        if (bank != null &&
                (Constants.PayStyleConstants.XIANLOAN_COMPANY_BODY_FALG.equals(product.getCompanyBody())
                || (BorrowUtil.borrowFinalState(nowBorr) &&
                        Constants.PayStyleConstants.SXD_COMPANY_BODY_FALG.equals(product.getCompanyBody())))) {
            BindChannelsRequest b = new BindChannelsRequest();
            String appId = productCompanyExtMapper.selectValueByCompany(Constants.PayStyleConstants.XIANLOAN_COMPANY_BODY_FALG, Constants.PayStyleConstants.DC_BIND_CARD_APPID);
            b.setAppId(Integer.parseInt(appId));
            List<String> bankNumList = Collections.singletonList(bank.getBankNum());
            b.setBankCards(bankNumList);
            QueryBindChannelResp queryBindChannelResp = tradeService.queryBindChannel(b);
            log.info("老合同完成后查询用户主卡是否绑定过新产品 queryBindChannelResp" +queryBindChannelResp);
            if (queryBindChannelResp != null && queryBindChannelResp.getCode().equals("200")) {
                String[] status = ((JSONObject) queryBindChannelResp.getData()).getJSONArray(bank.getBankNum()).toArray(new String[0]);
               log.info("银行卡{}查询结果 status = {}",bank.getBankNum(),status);
                if (StringUtils.isEmpty(status[0])) {
                    //未绑定新渠道 清除所有老银行卡
                    bankMapper.updateStatusToLose(bank.getPerId());
                    personMapper.updatePersonToLoseBank(bank.getPerId());
                }
            }
        }
    }

    public static void main(String[] args) throws ParseException {

    }
}