package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class DayAndTime implements Parcelable {
    private String day;
    private String StartTime;
    private String EndTime;

    public DayAndTime(String day, String startTime, String endTime) {
        this.day = day;
        StartTime = startTime;
        EndTime = endTime;
    }

    protected DayAndTime(Parcel in) {
        day = in.readString();
        StartTime = in.readString();
        EndTime = in.readString();
    }

    public DayAndTime(){
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public static final Creator<DayAndTime> CREATOR = new Creator<DayAndTime>() {
        @Override
        public DayAndTime createFromParcel(Parcel in) {
            return new DayAndTime(in);
        }

        @Override
        public DayAndTime[] newArray(int size) {
            return new DayAndTime[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(day);
        parcel.writeString(StartTime);
        parcel.writeString(EndTime);
    }
}
