package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class ButtonEventModel implements Parcelable {
    private String patientID, time;
    private boolean operatorWarned, reviewed;

    public ButtonEventModel() {
    }

    public ButtonEventModel(String patientID, String time, boolean operatorWarned, boolean reviewed) {
        this.patientID = patientID;
        this.time = time;
        this.operatorWarned = operatorWarned;
        this.reviewed = reviewed;
    }

    protected ButtonEventModel(Parcel in) {
        patientID = in.readString();
        time = in.readString();
        operatorWarned = in.readByte() != 0;
        reviewed = in.readByte() != 0;
    }

    public static final Creator<ButtonEventModel> CREATOR = new Creator<ButtonEventModel>() {
        @Override
        public ButtonEventModel createFromParcel(Parcel in) {
            return new ButtonEventModel(in);
        }

        @Override
        public ButtonEventModel[] newArray(int size) {
            return new ButtonEventModel[size];
        }
    };

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isOperatorWarned() {
        return operatorWarned;
    }

    public void setOperatorWarned(boolean operatorWarned) {
        this.operatorWarned = operatorWarned;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(patientID);
        parcel.writeString(time);
        parcel.writeByte((byte) (operatorWarned ? 1 : 0));
        parcel.writeByte((byte) (reviewed ? 1 : 0));
    }
}
