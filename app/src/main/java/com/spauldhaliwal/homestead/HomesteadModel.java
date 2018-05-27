package com.spauldhaliwal.homestead;

/**
 * Created by pauldhaliwal on 2018-03-26.
 */

public class HomesteadModel {

    private String uId;
    private String name;
    private String[] users;

    public HomesteadModel() {
    }

    public HomesteadModel(String uId, String name) {
        this.uId = uId;
        this.name = name;
    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

    public String[] getUsers() {
        return users;
    }
}
