package com.jhh.dc.loan.manage.service.operations;

import com.jhh.dc.loan.manage.service.excel.ImportJDCardExcelEntity;

import java.util.List;

public interface ElectronicCardService {

    /**
     * 导入京东电子卡
     * @returnF
     */
    int importJDCardExcel(List<ImportJDCardExcelEntity> jdCardList) throws Exception;
}
