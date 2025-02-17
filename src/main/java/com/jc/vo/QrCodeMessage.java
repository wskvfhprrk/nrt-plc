package com.jc.vo;

import java.util.Date;

public class QrCodeMessage {
    private String type = "QR_CODE";
    private String qrCodeUrl;
    private Date timestamp;

    public QrCodeMessage() {
    }

    public QrCodeMessage(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
        this.timestamp = new Date();
    }

    public String getType() {
        return type;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
