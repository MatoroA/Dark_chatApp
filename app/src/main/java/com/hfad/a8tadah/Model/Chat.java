package com.hfad.a8tadah.Model;

import java.io.Serializable;

public class Chat implements Serializable {

    private User user;
    private String chatId;
    private String lastMessge;
    int unReadCount;

    public Chat(){}

    public Chat(String chatId, User user){
        this.chatId = chatId;
        this.user = user;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setLastMessge(String lastMessge) {
        this.lastMessge = lastMessge;
    }

    public String getLastMessge() {
        return lastMessge;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
