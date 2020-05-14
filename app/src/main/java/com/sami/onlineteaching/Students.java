package com.sami.onlineteaching;

public class Students {

    String fullName, username, level, target;

    public Students(String fullName, String username, String level, String target) {
        this.fullName = fullName;
        this.username = username;
        this.level = level;
        this.target = target;
    }

    public Students() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
