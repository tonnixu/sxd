package com.jhh.dc.loan.api.channel;

/**
 * 2018/3/30.
 */
public interface AgentFactory {

    <T extends AgentChannelService> T create(String payChannel);
}
