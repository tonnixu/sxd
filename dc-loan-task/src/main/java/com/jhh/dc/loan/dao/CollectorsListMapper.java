package com.jhh.dc.loan.dao;

import org.apache.ibatis.annotations.Param;

import com.jhh.dc.loan.entity.loan.CollectorsList;
import com.jhh.dc.loan.entity.loan.ExportWorkReport;
import com.jhh.dc.loan.entity.manager.CollectorsCompanyVo;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;


public interface CollectorsListMapper extends Mapper<CollectorsList> {


    /**
     * 批量分配给特殊
     * @param list
     * @return
     */
    int batchInsertCollectorsList(@Param("list") List<CollectorsList> list);

}
