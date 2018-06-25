package com.spauldhaliwal.homestead;

class JobNote {

    private String id;
    private String creatorId;
    private String creatorIdImage;
    private String content;

    public JobNote() {
    }

    public JobNote(String id, String creatorId, String creatorIdImage, String content) {
        this.id = id;
        this.creatorId = creatorId;
        this.creatorIdImage = creatorIdImage;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getCreatorIdImage() {
        return creatorIdImage;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "JobNote{" +
                "id='" + id + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", creatorIdImage='" + creatorIdImage + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
