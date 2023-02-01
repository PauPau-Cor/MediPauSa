package com.example.proyectoceti.ClassesAndModels;

import java.util.Date;

public class NotificationModel {
    String ReceiverID;
    String Title;
    String Body;
    Date TimeStamp;

    public NotificationModel() {
    }

    public NotificationModel(String receiverID, String title, String body, Date timeStamp) {
        ReceiverID = receiverID;
        Title = title;
        Body = body;
        TimeStamp = timeStamp;
    }

    public String getReceiverID() {
        return ReceiverID;
    }

    public void setReceiverID(String receiverID) {
        ReceiverID = receiverID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }

    public Date getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        TimeStamp = timeStamp;
    }
}
