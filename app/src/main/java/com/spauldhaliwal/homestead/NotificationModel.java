package com.spauldhaliwal.homestead;

import java.io.Serializable;

public class NotificationModel implements Serializable {

        private String message;
        private String notificationId;
        private String senderUid;

    public NotificationModel(String message, String notificationId, String senderUid) {
        this.message = message;
        this.notificationId = notificationId;
        this.senderUid = senderUid;
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


    @Override
    public String toString() {
        return "NotificationModel{" +
                "message='" + message + '\'' +
                ", notificationId='" + notificationId + '\'' +
                ", senderUid='" + senderUid + '\'' +
                '}';
    }
}
