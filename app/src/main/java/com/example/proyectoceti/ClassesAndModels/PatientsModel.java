package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientsModel implements Parcelable {


    private List<String> CareUserIDs = null;
    private String FamUserID;
    private String PatientID;
    private HashMap<String, Object> PatientInfo = null;
    private HashMap<String, Object> Hospital = null;
    private HashMap<String, Object> Insurance = null;

    public PatientsModel() {
    }

    public PatientsModel(List<String> CareUserIDs, String FamUserID, String PatientID, HashMap<String, Object> PatientInfo, HashMap<String, Object> Hospital, HashMap<String, Object> Insurance){
        this.CareUserIDs=CareUserIDs;
        this.FamUserID =FamUserID;
        this.PatientID=PatientID;
        this.PatientInfo=PatientInfo;
        this.Hospital= Hospital;
        this.Insurance = Insurance;
    }

    protected PatientsModel(Parcel in) {
        CareUserIDs = in.createStringArrayList();
        FamUserID = in.readString();
        PatientID = in.readString();
        PatientInfo = (HashMap<String, Object>) in.readSerializable();
        Hospital = (HashMap<String, Object>) in.readSerializable();
        Insurance = (HashMap<String, Object>) in.readSerializable();
    }

    public static final Creator<PatientsModel> CREATOR = new Creator<PatientsModel>() {
        @Override
        public PatientsModel createFromParcel(Parcel in) {
            return new PatientsModel(in);
        }

        @Override
        public PatientsModel[] newArray(int size) {
            return new PatientsModel[size];
        }
    };

    public List<String> getCareUserIDs() {return CareUserIDs;}

    public void setCareUserIDs(List<String> careUserIDs) {CareUserIDs = careUserIDs;}

    public String getFamUserID() {
        return FamUserID;
    }

    public void setFamUserID(String famUserID) {
        FamUserID = famUserID;
    }

    public String getPatientID() {
        return PatientID;
    }

    public void setPatientID(String patientID) {
        PatientID = patientID;
    }

    public Map<String, Object> getPatientInfo() {
        return PatientInfo;
    }

    public void setPatientInfo(HashMap<String, Object> patientInfo) {
        PatientInfo = patientInfo;
    }

    public HashMap<String, Object> getHospital() {
        return Hospital;
    }

    public void setHospital(HashMap<String, Object> hospital) {
        Hospital = hospital;
    }

    @PropertyName("Insurance")
    public HashMap<String, Object> getInsurance() {
        return Insurance;
    }

    @PropertyName("Insurance")
    public void setInsurance(HashMap<String, Object> insurance) {
        Insurance = insurance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(CareUserIDs);
        parcel.writeString(FamUserID);
        parcel.writeString(PatientID);
        parcel.writeSerializable(PatientInfo);
        parcel.writeSerializable(Hospital);
        parcel.writeSerializable(Insurance);
    }
}
