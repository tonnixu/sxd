package com.jhh.dc.loan.mapper.gen;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.api.entity.capital.TradeVo;
import com.jhh.dc.loan.entity.OrderExt;
import org.apache.ibatis.annotations.Param;

import com.jhh.dc.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.dc.loan.mapper.gen.domain.LoanOrderDOExample;

public interface LoanOrderDOMapper {
    int countByExample(LoanOrderDOExample example);

    int deleteByExample(LoanOrderDOExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LoanOrderDO record);

    int insertSelective(LoanOrderDO record);

    List<LoanOrderDO> selectByExample(LoanOrderDOExample example);

    LoanOrderDO selectByPrimaryKey(Integer id);

    LoanOrderDO selectSubOrderByPid(Integer pid);

    Double getListByBorrId(Integer borrId);

    LoanOrderDO selectBySerNo(@Param("serNo") String serNo);

    int updateByExampleSelective(@Param("record") LoanOrderDO record, @Param("example") LoanOrderDOExample example);

    int updateByExample(@Param("record") LoanOrderDO record, @Param("example") LoanOrderDOExample example);

    int updateByPrimaryKeySelective(LoanOrderDO record);

    int updateByPrimaryKey(LoanOrderDO record);

    int updateStatusById(@Param("orderId")Integer orderId,@Param("status") String status,@Param("rlRemark") String msg);

    int updateChannelsBySerialNo(@Param("map") Map<String,Object> map);

    void updateStatusBySerialNo(@Param("serialNo")String serialNo,@Param("status") String status,@Param("rlRemark") String msg);

    void updateServiceOrderStatusBySerialNo(@Param("serialNo")String serialNo, @Param("status") String status, @Param("rlRemark") String msg);

    int updateOrderByChannel(@Param("ext") List<OrderExt> ext,@Param("channel") String channel);

    int updateOrderByFail(@Param("map") Map map);

}