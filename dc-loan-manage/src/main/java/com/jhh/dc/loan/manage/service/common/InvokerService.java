package com.jhh.dc.loan.manage.service.common;

import com.jhh.dc.loan.entity.app.NoteResult;

public interface InvokerService {

    /**
     * 调用各个组件
     * @return
     */
    NoteResult invokeComponent();
}
