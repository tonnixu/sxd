package com.jhh.dc.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.dc.loan.api.app.RiskService;
import com.jhh.dc.loan.constant.Constant;
import com.jinhuhang.risk.client.dto.QueryResultDto;
import com.jinhuhang.risk.client.dto.*;
import com.jhh.dc.loan.constant.Constant;
import com.jinhuhang.risk.client.dto.QueryResultDto;
import com.jinhuhang.risk.client.service.impl.blacklist.BlacklistAPIClient;
import org.springframework.beans.factory.annotation.Value;



/**
 * 认证service
 *
 * @author xuepengfei
 */
@Service
public class RiskServiceImpl implements RiskService {

    private BlacklistAPIClient blacklistAPIClient = new BlacklistAPIClient();

    private static final Logger logger = LoggerFactory.getLogger(RiskServiceImpl.class);

    @Value("${productId}")
    private String productId;

    @Override
    public boolean isBlack(String phone,String idCard) {
        try {
            // 调用风控黑名单新接口，添加手机号码
            logger.info("调用风控黑名单接口参数，身份证号= 【" + idCard + "】, 手机号 = 【" + phone + "】");
            QueryResultDto queryResultDto = blacklistAPIClient.blacklistSingleQuery(idCard, phone);
            logger.info("调用风控黑名单接口返回，"+ JSONObject.toJSONString(queryResultDto));

            // 异常
            if(Constant.ERROR_CODE.equals(queryResultDto.getCode())){
                return true;
            }

            if (Constant.IS_BLACK.equals(String.valueOf(queryResultDto.getModel()))){
                return false;
            }else {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("调用风控黑名单接口失败，身份证号= 【" + idCard + "】, 手机号 = 【" + phone + "】");
            return true;
        }

    }


}
