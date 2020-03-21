package com.hfad.a8tadah.Model;

public class Message {
    private String messageId, message, sender, chatId;
    private boolean isOpened, isSent;
    private Long timeStamp;

    public Message(){

    }

    public Message(String messageId, String message, String sender,
                   Long timeStamp, String chatId, boolean isSent, boolean isOpened){
        this.messageId = messageId;
        this.message = message;
        this.sender = sender;
        this.isOpened = isOpened;
        this.timeStamp = timeStamp;
        this.isSent = isSent;
        this.chatId = chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSent() {
        return isSent;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public boolean getIsOpened(){
        return isOpened;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }
}
