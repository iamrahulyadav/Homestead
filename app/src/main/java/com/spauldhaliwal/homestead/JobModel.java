package com.spauldhaliwal.homestead;

import java.io.Serializable;

/**
 * Created by pauldhaliwal on 2018-03-07.
 */

class JobModel implements Serializable {

    private String id;
    private String name;
    private String creatorId;
    private String creatorIdImage;
    private String description;
    private int status;
    private String owner;
//    private long dateAdded;
    private boolean isPrivate;
    private String sortOrder;


    public JobModel() {
    }

    public JobModel(String id, String name, String description, String creatorId, String creatorIdImage , int status, String owner ,boolean isPrivate, String sortOrder) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.creatorIdImage = creatorIdImage;
        this.description = description;
//        this.dateAdded = dateAdded;
        this.isPrivate = isPrivate;
        this.sortOrder = sortOrder;
    }

    public JobModel(String id, String name, String description, int status, String owner, boolean isPrivate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    public JobModel(String id, boolean isPrivate) {
        this.id = id;
        this.isPrivate = isPrivate;
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getStatus() {
        return status;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isIsPrivate() {
        return isPrivate;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    @Override
    public String toString() {
        return "JobModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", creatorIdImage='" + creatorIdImage + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", owner='" + owner + '\'' +
                ", isPrivate=" + isPrivate +
                '}';
    }

}
