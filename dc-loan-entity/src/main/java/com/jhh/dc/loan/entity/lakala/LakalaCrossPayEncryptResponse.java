package com.jhh.dc.loan.entity.lakala;

/**
 * 拉卡拉跨境支付加密响应报文
 * 2.0.0版本报文 ，没有reqType字段
 */
public class LakalaCrossPayEncryptResponse extends LakalaCrossPaySuperResponse {

    private static final long serialVersionUID = -8037859172913791796L;

    private String encData;

    private String encKey;

    private String mac;

    public String getEncData() {
        return encData;
    }

    public void setEncData(String encData) {
        this.encData = encData;
    }

    public String getEncKey() {
        return encKey;
    }

    public void setEncKey(String encKey) {
        this.encKey = encKey;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LakalaCrossPayEncryptResponse{");
        sb.append("super='").append(super.toString()).append('\'');
        sb.append(", encData='").append(encData).append('\'');
        sb.append(", encKey='").append(encKey).append('\'');
        sb.append(", mac='").append(mac).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
