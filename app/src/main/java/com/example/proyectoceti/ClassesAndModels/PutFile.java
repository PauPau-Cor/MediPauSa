package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class PutFile implements Parcelable {
    public String name;
    public String url;

    public PutFile() {
    }

    public PutFile(String name, String url) {
        this.name = name;
        this.url = url;
    }

    protected PutFile(Parcel in) {
        name = in.readString();
        url = in.readString();
    }

    public static final Creator<PutFile> CREATOR = new Creator<PutFile>() {
        @Override
        public PutFile createFromParcel(Parcel in) {
            return new PutFile(in);
        }

        @Override
        public PutFile[] newArray(int size) {
            return new PutFile[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(url);
    }
}
