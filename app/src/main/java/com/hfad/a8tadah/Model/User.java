package com.hfad.a8tadah.Model;

import java.io.Serializable;

public class User implements Serializable {

    private String imageUrl, userName, userId,
                    contact, currentChatId, name, status;
    private long lastSeen;
    private boolean isOnline;

    public User(){}

    public User(String imageUrl, String userName, String name, String userId,
                String contact, String status, long lastSeen){
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.userId = userId;
        this.contact = contact;
        this.name = name;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCurrentChatId(String currentChatId) {
        this.currentChatId = currentChatId;
    }

    public String getCurrentChatId() {
        return currentChatId;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
