package com.sami.onlineteaching;

public class Feedbacks {

    private String uid, name, comment;

    public Feedbacks() {
    }

    public Feedbacks(String uid, String name, String comment) {
        this.uid = uid;
        this.name = name;
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
