package com.nhom12.ourquizlet.data.model;

import java.util.List;

public class Folder {
    private String id;
    private String idCreator;
    private List<String> idTopics;
    private String name;
    private String avatar;
    private String username;

    public Folder(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdCreator() {
        return idCreator;
    }

    public void setIdCreator(String idCreator) {
        this.idCreator = idCreator;
    }

    public List<String> getIdTopics() {
        return idTopics;
    }

    public void setIdTopics(List<String> idTopics) {
        this.idTopics = idTopics;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
