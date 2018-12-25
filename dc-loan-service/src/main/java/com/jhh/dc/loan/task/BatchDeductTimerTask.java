package com.jhh.dc.loan.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jhh.dc.loan.api.channel.TradePayService;
import com.jhh.dc.loan.api.entity.capital.TradeBatchVo;
import com.jhh.dc.loan.api.entity.capital.TradeVo;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  单线程池做定时任务批量代扣
 */
public class BatchDeductTimerTask  implements Runnable {

	private ScheduledExecutorService scheduled_executor_single;

	private Queue<List<TradeVo>> queue;
	private TradePayService tradePayService;
	private String payChannel;
	private String optPerson;

	public BatchDeductTimerTask(Queue<List<TradeVo>> queue, String payChannel, String optPerson, TradePayService tradePayService) {
		this.queue = queue;
		this.tradePayService = tradePayService;
		this.payChannel = payChannel;
		this.optPerson = optPerson;
	}

	public BatchDeductTimerTask() {
		this.scheduled_executor_single = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("batchDeductExecutor-sinle").build());
	}
	@Override
	public void run() {
		if (!queue.isEmpty()){
			List<TradeVo> poll = queue.poll();
			if (poll != null && poll.size() > 0 ){
				TradeBatchVo tradeBatchVo = new TradeBatchVo(poll, poll.size(), payChannel, optPerson,poll.get(0).getAppId());
				tradePayService.batchDeduct(tradeBatchVo);
			}
		}else {
			destroy();
		}
	}


	public void delayScheduleAtFixedRate(Runnable runner, long initialDelay, long period, TimeUnit unit) {
		scheduled_executor_single.scheduleAtFixedRate(runner, initialDelay,period, unit);
	}

	private void destroy(){
		scheduled_executor_single.shutdown();
	}
}
