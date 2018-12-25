package com.jhh.dc.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jhh.dc.loan.api.app.BQSService;
import com.jhh.dc.loan.api.white.RiskWhiteService;
import com.jhh.dc.loan.common.util.HttpUrlPost;
import com.jhh.dc.loan.common.util.PropertiesReaderUtil;
import com.jhh.dc.loan.mapper.app.CodeValueMapper;
import com.jinhuhang.risk.client.dto.QueryResultDto;
import com.jinhuhang.risk.client.dto.bqs.jsonbean.BQSRequestBean;
import com.jinhuhang.risk.client.service.impl.bqs.BQSApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;


/**
 * 白骑士公共服务
 *
 * @author xuepengfei
 */
@Service
@Slf4j
public class BQSServiceImpl implements BQSService {

    private static final Logger logger = LoggerFactory.getLogger(BQSServiceImpl.class);

    @Value("${productId}")
    private String productId;

    @Autowired
    private RiskWhiteService riskWhiteService;

    @Override
    public boolean runBQS(String phone, String name, String idNumber, String event, String tokenKey, String device) {
        //调用白名单接口查看用户是否在白名单
        if (riskWhiteService.isWhite(phone)) {
            return true;
        }
        BQSApiClient bqsApiClient = new BQSApiClient();
        try {
            BQSRequestBean bqsRequestBean = new BQSRequestBean();
            bqsRequestBean.setCertNo(idNumber);
            bqsRequestBean.setEventType(event);
            bqsRequestBean.setMobile(phone);
            bqsRequestBean.setName(name);
            bqsRequestBean.setTokenKey(tokenKey);
            bqsRequestBean.setRequestId(String.valueOf(System.currentTimeMillis()));
            bqsRequestBean.setPlatform(device);
            logger.info("白骑士请求参数：bqsRequestBean：" + JSONObject.toJSONString(bqsRequestBean));

            QueryResultDto bqsRisk = bqsApiClient.getBQSRisk(productId, bqsRequestBean);

            logger.info("白骑士返回参数：bqsRisk：" + JSONObject.toJSONString(bqsRisk));
            return "1".equals(bqsRisk.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用白骑士出现异常，请求全部通过");
            return true;
        }
    }
}
