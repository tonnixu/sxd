package com.jhh.dc.loan.service.black;

import com.alibaba.fastjson.JSON;
import com.jhh.dc.loan.api.app.RiskService;
import com.jhh.dc.loan.api.black.RiskBlackService;
import com.jhh.dc.loan.api.constant.StateCode;
import com.jhh.dc.loan.common.util.CodeReturn;
import com.jhh.dc.loan.constant.Constant;
import com.jhh.dc.loan.entity.app.NoteResult;
import com.jhh.dc.loan.entity.common.Constants;
import com.jhh.dc.loan.mapper.app.BorrowListMapper;
import com.jinhuhang.risk.client.dto.QueryResultDto;
import com.jinhuhang.risk.client.dto.blacklist.jsonbean.BlackListDto;
import com.jinhuhang.risk.client.service.impl.blacklist.BlacklistAPIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Calendar;

@Slf4j
public class RiskBlackServiceImpl implements RiskBlackService {

    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private RiskService riskService;

    private BlacklistAPIClient riskClient = new BlacklistAPIClient();

    private static final String RISK_SUCCESS = "1";

    /**
     * 调用风控拉黑
     */
    public NoteResult invokeRisk(String phone, String cardNum, String personName) {
        try {
            BlackListDto dto = new BlackListDto();
            dto.setName(personName);
            dto.setSys("dc_loan");
            dto.setIdcard(cardNum);
            dto.setPhone(phone);
            dto.setHandlerNo("9999");
            dto.setHandlerName("特殊");
            dto.setType(Constants.UserBlockWhite.BLACK);
            dto.setReason("清结算调用业务端拉黑");
            dto.setCreateTime(Calendar.getInstance().getTime());

            log.info("------->调用风控拉黑/洗白接口 入参【%s】", JSON.toJSONString(dto));

            QueryResultDto resultDto = riskClient.blacklist(dto);

            log.info("------->调用风控拉黑 结果 = {}", resultDto);
            return NoteResult.SUCCESS_RESPONSE("操作成功");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用风控拉黑失败，异常信息 =  {}", e);
            return NoteResult.FAIL_RESPONSE(e.getMessage());
        }

    }

    @Override
    public NoteResult black(String phone, String cardNum, String personName, Integer perId) {

        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(cardNum)) {
            log.info("定时任务拉黑请求入参异常，phone = {},cardNum = {}", phone, cardNum);
            return NoteResult.FAIL_RESPONSE(StateCode.PARAM_EMPTY_MSG);
        }

        if (!riskService.isBlack(phone, cardNum)) {
            return new NoteResult(CodeReturn.FAIL_CODE, StateCode.BLACKLIST_CODE_MSG);
        }

        return invokeRisk(phone, cardNum, personName);
    }
}
