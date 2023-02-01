package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactModel implements Parcelable {
    private String Name, CellphoneNumber, PatientID;

    public ContactModel() {
    }

    public ContactModel(String name, String cellphoneNumber, String patientID) {
        Name = name;
        CellphoneNumber = cellphoneNumber;
        PatientID = patientID;
    }

    protected ContactModel(Parcel in) {
        Name = in.readString();
        CellphoneNumber = in.readString();
        PatientID = in.readString();
    }

    public static final Creator<ContactModel> CREATOR = new Creator<ContactModel>() {
        @Override
        public ContactModel createFromParcel(Parcel in) {
            return new ContactModel(in);
        }

        @Override
        public ContactModel[] newArray(int size) {
            return new ContactModel[size];
        }
    };

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCellphoneNumber() {
        return CellphoneNumber;
    }

    public void setCellphoneNumber(String cellphoneNumber) {
        CellphoneNumber = cellphoneNumber;
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
        parcel.writeString(Name);
        parcel.writeString(CellphoneNumber);
        parcel.writeString(PatientID);
    }
}
