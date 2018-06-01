package com.spauldhaliwal.homestead;

import java.io.Serializable;

public class NotificationModel implements Serializable {

    private String from;
    private String title;
    private String body;
    private String notificationId;
    private String senderUid;
    private String type;

    public NotificationModel(String from, String title, String body, String notificationId, String senderUid, String type) {
        this.from = from;
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
        this.senderUid = senderUid;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getType() {
        return type;
    }
}
