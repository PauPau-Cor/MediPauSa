package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class EventModel implements Parcelable {
    private String type, date, time, name, description, patientID, ExtraID;
    private Date timeStamp;

    public EventModel(){

    }

    public EventModel(String Type, Date TimeStamp, String Date, String Time, String Name, String Description, String PatientID) {
        type = Type;
        timeStamp = TimeStamp;
        date = Date;
        time = Time;
        name = Name;
        description = Description;
        patientID = PatientID;
    }

    public EventModel(String Type, Date TimeStamp, String Date, String Time, String Name, String Description, String PatientID, String extraID) {
        type = Type;
        timeStamp = TimeStamp;
        date = Date;
        time = Time;
        name = Name;
        description = Description;
        patientID = PatientID;
        ExtraID = extraID;
    }

    protected EventModel(Parcel in) {
        type = in.readString();
        timeStamp = new Date(in.readLong());
        date = in.readString();
        time = in.readString();
        name = in.readString();
        description = in.readString();
        patientID = in.readString();
        ExtraID = in.readString();
    }

    public static final Creator<EventModel> CREATOR = new Creator<EventModel>() {
        @Override
        public EventModel createFromParcel(Parcel in) {
            return new EventModel(in);
        }

        @Override
        public EventModel[] newArray(int i) {
            return new EventModel[0];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getExtraID() {
        return ExtraID;
    }

    public void setExtraID(String extraID) {
        ExtraID = extraID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeLong(timeStamp.getTime());
        parcel.writeString(date);
        parcel.writeString(time);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(patientID);
        parcel.writeString(ExtraID);
    }
}
