package com.jhh.dc.loan.dao;


import com.jhh.dc.loan.entity.manager.CodeValue;

import java.util.List;

public interface CodeValueMapper {

    List<CodeValue> getCodeValueListByCode(String code_type);

    CodeValue getCodeValueByCode(String code_type);
}