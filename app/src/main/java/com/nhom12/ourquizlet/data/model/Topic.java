package com.nhom12.ourquizlet.data.model;

import com.google.firebase.firestore.PropertyName;

public class Topic implements Cloneable{
    private String id;
    private String idUser;
    private String idCategory;
    private String username;
    private String title;
    private String description;
    private boolean isPublic = false;
    private int numberWord = 0;
    private int percent = 0;
    private int count = 0;
    private String avatar;

    public Topic() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(String idCategory) {
        this.idCategory = idCategory;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName ("isPublic")
    public boolean isPublic() {
        return isPublic;
    }

    @PropertyName ("isPublic")
    public void setPublic(boolean aPublic) {
        this.isPublic = aPublic;
    }

    public int getNumberWord() {
        return numberWord;
    }

    public void setNumberWord(int numberWord) {
        this.numberWord = numberWord;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public Topic clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (Topic) super.clone ();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError ();
        }
    }
}
