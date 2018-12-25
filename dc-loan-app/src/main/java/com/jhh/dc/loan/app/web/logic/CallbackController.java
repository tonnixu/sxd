package com.jhh.dc.loan.app.web.logic;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.dc.loan.api.channel.AgentBatchStateService;
import com.jhh.dc.loan.api.channel.AgentChannelService;
import com.jhh.dc.loan.api.entity.agreement.BatchCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback")
@Slf4j
public class CallbackController {


	@Reference
	private AgentBatchStateService agentBatchStateService;

	@Reference
	private AgentChannelService agentChannelService;


	/**
	 * 支付中心批量代扣回调
	 * @param callback
	 */
	@RequestMapping("/batchDeduct")
	public void BatchDeductCallback(@RequestBody BatchCallback callback){
		agentBatchStateService.batchCallback(callback);
	}

}
