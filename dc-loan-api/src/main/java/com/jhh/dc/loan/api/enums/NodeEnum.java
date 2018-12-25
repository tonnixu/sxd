package com.jhh.dc.loan.api.enums;

/**
 * 2018/7/13.
 */
public enum NodeEnum {


    /**用户未登录*/
    UNLOGIN_NODE("NS001"),
    /**设置支付密码*/
    SETPAYPWD_NODE("NS002"),
    /**用户未银行卡认证*/
    BANKCARD_NODE("NS003"),
    /**用户未签约*/
    UNSIGN_NODES("NS004"),
    /**跳转详情页进行操作*/
    DETAILS_NODES("NS005"),



    /**未知节点*/
    ERROR_NODES("NS999"),;

    private String node;

    private NodeEnum(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }


    public static boolean contains(String rulerName) {
        NodeEnum[] properties = values();
        for (NodeEnum property : properties) {
            if (property.name().equals(rulerName)) {
                return true;
            }
        }
        return false;
    }
}
