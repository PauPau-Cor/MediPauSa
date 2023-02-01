package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class Frequency implements Parcelable {
    private String freq;
    private Boolean taken;

    public Frequency() {
    }

    public Frequency(String freq, Boolean taken) {
        this.freq = freq;
        this.taken = taken;
    }

    protected Frequency(Parcel in) {
        freq = in.readString();
        byte tmpTaken = in.readByte();
        taken = tmpTaken == 0 ? null : tmpTaken == 1;
    }

    public static final Creator<Frequency> CREATOR = new Creator<Frequency>() {
        @Override
        public Frequency createFromParcel(Parcel in) {
            return new Frequency(in);
        }

        @Override
        public Frequency[] newArray(int size) {
            return new Frequency[size];
        }
    };

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public Boolean getTaken() {
        return taken;
    }

    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(freq);
        parcel.writeByte((byte) (taken == null ? 0 : taken ? 1 : 2));
    }
}
