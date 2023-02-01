package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class NeedModel implements Parcelable{
    private String Description, NeedDate;
    private String PatientID;
    private List<Frequency> Frequencies;

    public NeedModel(){

    }

    public NeedModel(String description, String needDate, String patientID, List<Frequency> frequencies) {
        Description = description;
        NeedDate = needDate;
        PatientID = patientID;
        Frequencies = frequencies;
    }

    protected NeedModel(Parcel in) {
        Description = in.readString();
        NeedDate = in.readString();
        PatientID = in.readString();
        Frequencies = in.createTypedArrayList(Frequency.CREATOR);
    }

    public static final Parcelable.Creator<NeedModel> CREATOR = new Parcelable.Creator<NeedModel>() {
        @Override
        public NeedModel createFromParcel(Parcel in) {
            return new NeedModel(in);
        }

        @Override
        public NeedModel[] newArray(int size) {
            return new NeedModel[size];
        }
    };

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getNeedDate() {
        return NeedDate;
    }

    public void setNeedDate(String needDate) {
        NeedDate = needDate;
    }

    public List<Frequency> getFrequencies() {
        return Frequencies;
    }

    public void setFrequencies(List<Frequency> frequencies) {
        Frequencies = frequencies;
    }

    public String getPatientID() {
        return PatientID;
    }

    public void setPatientID(String patientID) {
        PatientID = patientID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Description);
        parcel.writeString(NeedDate);
        parcel.writeString(PatientID);
        parcel.writeTypedList(Frequencies);
    }
}
