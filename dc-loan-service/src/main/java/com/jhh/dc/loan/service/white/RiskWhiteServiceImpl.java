package com.jhh.dc.loan.service.white;

import com.alibaba.fastjson.JSON;

import com.jhh.dc.loan.api.white.RiskWhiteService;

import com.jhh.dc.loan.entity.manager.CodeValue;
import com.jhh.dc.loan.mapper.app.CodeValueMapper;
import com.jinhuhang.risk.client.dto.QueryResultDto;

import com.jinhuhang.risk.client.service.impl.whitelist.WhitelistAPIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;


@Slf4j
public class RiskWhiteServiceImpl implements RiskWhiteService {

    private WhitelistAPIClient whitelistAPIClient = new WhitelistAPIClient();

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Override
    public boolean isWhite(String phone) {
        return isWhite(new String[]{phone})[0];
    }

    @Override
    public boolean[] isWhite(String... phone) {
        boolean[] returnVal = new boolean[phone.length];
        try {
            CodeValue codeValue = codeValueMapper.selectByCodeType("white_directory_productId");
            String productId = codeValue.getCodeCode();

            log.info("调用白名单接口入参 productId = {}, params = {}", productId, JSON.toJSONString(phone));
            QueryResultDto result = whitelistAPIClient.exsitsWhiteData(Integer.parseInt(productId), JSON.toJSONString(phone));

            // 调用风控黑名单接口失败|或者返回的结果为空，这里做返回
            if (ObjectUtils.isEmpty(result)) {
                return returnVal;
            }

            if (!"0000".equals(result.getCode())) {
                return returnVal;
            }

            log.info("调用白名单接口结果 result = {}", result.getModel());
            String code = (String) result.getModel();

            if (ObjectUtils.isEmpty(code) || "null".equals(code)) {
                return returnVal;
            }

            for (int i = 0; i < phone.length; i++) {
                if ('1' == code.charAt(i)) {
                    returnVal[i] = true;
                }
            }
        } catch (Exception e) {
            log.error("调用风控查询白名单接口异常", e);
            return returnVal;
        }
        return returnVal;
    }
}
