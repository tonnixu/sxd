package com.jhh.dc.loan.config;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.jhh.dc.loan.common.util.RedisConst;
import com.jhh.dc.loan.mapper.app.CodeValueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;

/**
 * 2018/11/15. 启动时加载类
 */
@Component
public class StartupLoading {

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CodeValueMapper codeValueMapper;

    /**
     *  启动时加载用户每日申请次数放入缓存
     */
    @PostConstruct
    public void getApplyLimit(){
        //获取限制次数
        String limit = codeValueMapper.selectCodeByType("apply_limit");
        if (StringUtils.isEmpty(limit)){
            limit = "3";
        }
        jedisCluster.setnx(RedisConst.DC_LOAN_APPLY_MAX_NUMBER_KEY,limit);
    }
}
