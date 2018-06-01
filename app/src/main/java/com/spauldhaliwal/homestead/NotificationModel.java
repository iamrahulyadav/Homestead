package com.spauldhaliwal.homestead;

import java.io.Serializable;

public class NotificationModel implements Serializable {

    private String from;
    private String message;
    private String notificationId;
    private String senderUid;

    public NotificationModel(String from, String message, String notificationId, String senderUid) {
        this.from = from;
        this.message = message;
        this.notificationId = notificationId;
        this.senderUid = senderUid;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getSenderUid() {
        return senderUid;
    }
}
