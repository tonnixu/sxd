package com.jhh.dc.loan.manage.controller;

import com.jhh.dc.loan.entity.app.NoteResult;
import com.jhh.dc.loan.manage.service.common.InvokerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调用链实现
 */
@RestController
@RequestMapping("/manage")
public class InvokerController {

    @Autowired
    private InvokerService invokerService;

    @RequestMapping("/health")
    public NoteResult invoke() {
        return invokerService.invokeComponent();
    }


}
