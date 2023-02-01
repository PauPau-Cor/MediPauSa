package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MedsModel implements Parcelable {
    private String MedName, MedQuantity, ExpirationDate, Dosage, TreatmentEndDate;
    private String PatientID;
    private List<Frequency> Frequencies;
    private String NewMedQuantity, NewExpirationDate;

    public MedsModel(){

    }

    public MedsModel(String medName, String medQuantity, String expirationDate, String dosage, String treatmentEndDate, String patientID, List<Frequency> frequencies) {
        MedName = medName;
        MedQuantity = medQuantity;
        ExpirationDate = expirationDate;
        Dosage = dosage;
        TreatmentEndDate = treatmentEndDate;
        PatientID = patientID;
        Frequencies = frequencies;
    }

    public MedsModel(String medName, String medQuantity, String expirationDate, String dosage, String treatmentEndDate, String patientID, List<Frequency> frequencies, String newMedQuantity, String newExpirationDate) {
        MedName = medName;
        MedQuantity = medQuantity;
        ExpirationDate = expirationDate;
        Dosage = dosage;
        TreatmentEndDate = treatmentEndDate;
        PatientID = patientID;
        Frequencies = frequencies;
        NewMedQuantity = newMedQuantity;
        NewExpirationDate = newExpirationDate;
    }

    protected MedsModel(Parcel in) {
        MedName = in.readString();
        MedQuantity = in.readString();
        ExpirationDate = in.readString();
        Dosage = in.readString();
        TreatmentEndDate = in.readString();
        PatientID = in.readString();
        Frequencies = in.createTypedArrayList(Frequency.CREATOR);
        NewMedQuantity = in.readString();
        NewExpirationDate = in.readString();
    }

    public static final Creator<MedsModel> CREATOR = new Creator<MedsModel>() {
        @Override
        public MedsModel createFromParcel(Parcel in) {
            return new MedsModel(in);
        }

        @Override
        public MedsModel[] newArray(int size) {
            return new MedsModel[size];
        }
    };

    public String getMedName() {
        return MedName;
    }

    public void setMedName(String medName) {
        MedName = medName;
    }

    public String getMedQuantity() {
        return MedQuantity;
    }

    public void setMedQuantity(String medQuantity) {
        MedQuantity = medQuantity;
    }

    public String getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        ExpirationDate = expirationDate;
    }

    public String getDosage() {
        return Dosage;
    }

    public void setDosage(String dosage) {
        Dosage = dosage;
    }

    public String getTreatmentEndDate() {
        return TreatmentEndDate;
    }

    public void setTreatmentEndDate(String treatmentEndDate) {
        TreatmentEndDate = treatmentEndDate;
    }

    public String getPatientID() {
        return PatientID;
    }

    public void setPatientID(String patientID) {
        PatientID = patientID;
    }

    public List<Frequency> getFrequencies() {
        return Frequencies;
    }

    public void setFrequencies(List<Frequency> frequencies) {
        Frequencies = frequencies;
    }

    public String getNewMedQuantity() {
        return NewMedQuantity;
    }

    public void setNewMedQuantity(String newMedQuantity) {
        NewMedQuantity = newMedQuantity;
    }

    public String getNewExpirationDate() {
        return NewExpirationDate;
    }

    public void setNewExpirationDate(String newExpirationDate) {
        NewExpirationDate = newExpirationDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(MedName);
        parcel.writeString(MedQuantity);
        parcel.writeString(ExpirationDate);
        parcel.writeString(Dosage);
        parcel.writeString(TreatmentEndDate);
        parcel.writeString(PatientID);
        parcel.writeTypedList(Frequencies);
        parcel.writeString(NewMedQuantity);
        parcel.writeString(NewExpirationDate);
    }
}
