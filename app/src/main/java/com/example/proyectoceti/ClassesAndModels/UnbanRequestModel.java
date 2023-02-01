package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class UnbanRequestModel implements Parcelable {
    String Name, UserID, Reason;
    boolean Reviewed;

    public UnbanRequestModel() {
    }

    public UnbanRequestModel(String name, String userID, String reason, boolean reviewed) {
        Name = name;
        UserID = userID;
        Reason = reason;
        Reviewed = reviewed;
    }

    protected UnbanRequestModel(Parcel in) {
        Name = in.readString();
        UserID = in.readString();
        Reason = in.readString();
        Reviewed = in.readByte() != 0;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public boolean isReviewed() {
        return Reviewed;
    }

    public void setReviewed(boolean reviewed) {
        Reviewed = reviewed;
    }

    public static final Creator<UnbanRequestModel> CREATOR = new Creator<UnbanRequestModel>() {
        @Override
        public UnbanRequestModel createFromParcel(Parcel in) {
            return new UnbanRequestModel(in);
        }

        @Override
        public UnbanRequestModel[] newArray(int size) {
            return new UnbanRequestModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Name);
        parcel.writeString(UserID);
        parcel.writeString(Reason);
        parcel.writeByte((byte) (Reviewed ? 1 : 0));
    }
}
