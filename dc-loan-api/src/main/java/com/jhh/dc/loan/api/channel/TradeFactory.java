package com.jhh.dc.loan.api.channel;

/**
 * 2018/3/30.
 */
public interface TradeFactory {

    <T extends TradePayService> T createBean(String payChannel);
}
