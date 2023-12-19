package com.nhom12.ourquizlet.data.model;

public class CategoryTopic implements Cloneable {
    private String id;
    private String title;

    public CategoryTopic() {
    }

    public CategoryTopic(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public CategoryTopic(String id) {
        this.id = id;
        this.title = "TITLE";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CategoryTopic clone() {
        try {
            CategoryTopic clone = (CategoryTopic) super.clone ();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError ();
        }
    }
}
