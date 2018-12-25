package com.jhh.dc.loan.api.common;

import com.jhh.dc.loan.entity.app.NoteResult;

/**
 * 调度链服务
 */
public interface InvokerComponentService {

    /**
     * 调用各个组件
     * @return
     */
    NoteResult invokeComponent();
}
