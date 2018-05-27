package com.spauldhaliwal.homestead;

import java.io.Serializable;

public class MessageModel implements Serializable{
    private String message;
    private String messageUid;
    private String senderUid;
    private String senderName;
    private long timeSent;
    private String profileImage;

    public MessageModel() {
    }

    public MessageModel(String message, String messageUid, String senderUid, String senderName, long timeSent, String profileImage) {
        this.message = message;
        this.messageUid = messageUid;
        this.senderUid = senderUid;
        this.senderName = senderName;
        this.timeSent = timeSent;
        this.profileImage = profileImage;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageUid(String messageUid) {
        this.messageUid = messageUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "message='" + message + '\'' +
                ", messageUid='" + messageUid + '\'' +
                ", senderUid='" + senderUid + '\'' +
                ", senderName='" + senderName + '\'' +
                ", profileImage='" + profileImage + '\'' +
                '}';
    }
}
