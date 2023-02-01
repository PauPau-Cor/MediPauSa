package com.example.proyectoceti.ClassesAndModels;

import java.io.Serializable;
import java.util.Date;

public class ChatMessageModel implements Serializable {
    private String senderID;
    private String receiverID;
    private String message;
    private String dateTime;
    private String PFP;
    private String ConvoID;
    private String ConvoName;
    private boolean IsImage;
    private boolean IsFile;
    private Date dateObject;

    public ChatMessageModel() {
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPFP() {
        return PFP;
    }

    public void setPFP(String PFP) {
        this.PFP = PFP;
    }

    public String getConvoID() {
        return ConvoID;
    }

    public void setConvoID(String convoID) {
        ConvoID = convoID;
    }

    public String getConvoName() {
        return ConvoName;
    }

    public void setConvoName(String convoName) {
        ConvoName = convoName;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public boolean isImage() {
        return IsImage;
    }

    public void setImage(boolean image) {
        IsImage = image;
    }

    public boolean isFile() {
        return IsFile;
    }

    public void setFile(boolean file) {
        IsFile = file;
    }
}
