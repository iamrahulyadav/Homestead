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
//    private long dateAdded;
    private boolean isPrivate;


    public JobModel() {
    }

    public JobModel(String id, String name, String description, String creatorId, String creatorIdImage , int status ,boolean isPrivate) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.creatorIdImage = creatorIdImage;
        this.description = description;
//        this.dateAdded = dateAdded;
        this.isPrivate = isPrivate;
    }

    public JobModel(String id, String name, String description, boolean isPrivate) {
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

    public boolean isIsPrivate() {
        return isPrivate;
    }

}
