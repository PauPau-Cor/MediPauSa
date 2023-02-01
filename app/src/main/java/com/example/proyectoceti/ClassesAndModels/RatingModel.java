package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class RatingModel implements Parcelable {
    private String Critique, ReceiverID, SenderID;
    private Double Rating;

    public RatingModel() {
    }

    public RatingModel(String critique, String receiverID, String senderID, Double rating) {
        Critique = critique;
        ReceiverID = receiverID;
        SenderID = senderID;
        Rating = rating;
    }

    protected RatingModel(Parcel in) {
        Critique = in.readString();
        ReceiverID = in.readString();
        SenderID = in.readString();
        if (in.readByte() == 0) {
            Rating = null;
        } else {
            Rating = in.readDouble();
        }
    }

    public static final Creator<RatingModel> CREATOR = new Creator<RatingModel>() {
        @Override
        public RatingModel createFromParcel(Parcel in) {
            return new RatingModel(in);
        }

        @Override
        public RatingModel[] newArray(int size) {
            return new RatingModel[size];
        }
    };

    public String getCritique() {
        return Critique;
    }

    public void setCritique(String critique) {
        Critique = critique;
    }

    public String getReceiverID() {
        return ReceiverID;
    }

    public void setReceiverID(String receiverID) {
        ReceiverID = receiverID;
    }

    public String getSenderID() {
        return SenderID;
    }

    public void setSenderID(String senderID) {
        SenderID = senderID;
    }

    public Double getRating() {
        return Rating;
    }

    public void setRating(Double rating) {
        Rating = rating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Critique);
        parcel.writeString(ReceiverID);
        parcel.writeString(SenderID);
        if (Rating == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(Rating);
        }
    }
}
