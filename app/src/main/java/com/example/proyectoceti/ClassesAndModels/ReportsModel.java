package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class ReportsModel implements Parcelable {
    String SenderID, ReceiverID, Reason, ReceiverName, SenderName;
    boolean Reviewed;

    public ReportsModel(){

    }

    public ReportsModel(String senderID, String receiverID, String reason, String receiverName, String senderName, boolean reviewed) {
        SenderID = senderID;
        ReceiverID = receiverID;
        Reason = reason;
        ReceiverName = receiverName;
        SenderName = senderName;
        Reviewed = reviewed;
    }

    protected ReportsModel(Parcel in) {
        SenderID = in.readString();
        ReceiverID = in.readString();
        Reason = in.readString();
        ReceiverName = in.readString();
        SenderName = in.readString();
        Reviewed = in.readByte() != 0;
    }

    public String getSenderID() {
        return SenderID;
    }

    public void setSenderID(String senderID) {
        SenderID = senderID;
    }

    public String getReceiverID() {
        return ReceiverID;
    }

    public void setReceiverID(String receiverID) {
        ReceiverID = receiverID;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public String getReceiverName() {
        return ReceiverName;
    }

    public void setReceiverName(String receiverName) {
        ReceiverName = receiverName;
    }

    public String getSenderName() {
        return SenderName;
    }

    public void setSenderName(String senderName) {
        SenderName = senderName;
    }

    public boolean isReviewed() {
        return Reviewed;
    }

    public void setReviewed(boolean reviewed) {
        Reviewed = reviewed;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SenderID);
        dest.writeString(ReceiverID);
        dest.writeString(Reason);
        dest.writeString(ReceiverName);
        dest.writeString(SenderName);
        dest.writeByte((byte) (Reviewed ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReportsModel> CREATOR = new Creator<ReportsModel>() {
        @Override
        public ReportsModel createFromParcel(Parcel in) {
            return new ReportsModel(in);
        }

        @Override
        public ReportsModel[] newArray(int size) {
            return new ReportsModel[size];
        }
    };
}
