package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class LinkedUsersModel implements Parcelable {
    private boolean Active;
    private int ShiftsWorked;
    private double AccumulatedSalary;
    private String CareUserID;
    private String FamUserID;
    private String Name;
    private PutFile ProfilePicture;
    private boolean CareRated;
    private boolean FamRated;
    private double Salary;
    private int HoursWorked;
    private List<DayAndTime> Shifts;

    public LinkedUsersModel() {
    }

    public LinkedUsersModel(boolean active, int shiftsWorked, double accumulatedSalary, String careUserID, String famUserID, String name, PutFile profilePicture, boolean careRated, boolean famRated, double salary, int hoursWorked) {
        Active = active;
        ShiftsWorked = shiftsWorked;
        AccumulatedSalary = accumulatedSalary;
        CareUserID = careUserID;
        FamUserID = famUserID;
        Name = name;
        ProfilePicture = profilePicture;
        CareRated = careRated;
        FamRated = famRated;
        Salary = salary;
        HoursWorked = hoursWorked;
    }

    protected LinkedUsersModel(Parcel in) {
        Active = in.readByte() != 0;
        ShiftsWorked = in.readInt();
        AccumulatedSalary = in.readDouble();
        CareUserID = in.readString();
        FamUserID = in.readString();
        Name = in.readString();
        ProfilePicture = in.readParcelable(PutFile.class.getClassLoader());
        CareRated = in.readByte() != 0;
        FamRated = in.readByte() != 0;
        Salary = in.readDouble();
        HoursWorked = in.readInt();
        Shifts = in.createTypedArrayList(DayAndTime.CREATOR);
    }

    public static final Creator<LinkedUsersModel> CREATOR = new Creator<LinkedUsersModel>() {
        @Override
        public LinkedUsersModel createFromParcel(Parcel in) {
            return new LinkedUsersModel(in);
        }

        @Override
        public LinkedUsersModel[] newArray(int size) {
            return new LinkedUsersModel[size];
        }
    };

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public int getShiftsWorked() {
        return ShiftsWorked;
    }

    public void setShiftsWorked(int shiftsWorked) {
        ShiftsWorked = shiftsWorked;
    }

    public double getAccumulatedSalary() {
        return AccumulatedSalary;
    }

    public void setAccumulatedSalary(double accumulatedSalary) {
        AccumulatedSalary = accumulatedSalary;
    }

    public String getCareUserID() {
        return CareUserID;
    }

    public void setCareUserID(String careUserID) {
        CareUserID = careUserID;
    }

    public String getFamUserID() {
        return FamUserID;
    }

    public void setFamUserID(String famUserID) {
        FamUserID = famUserID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public PutFile getProfilePicture() {
        return ProfilePicture;
    }

    public void setProfilePicture(PutFile profilePicture) {
        ProfilePicture = profilePicture;
    }

    public boolean isCareRated() {
        return CareRated;
    }

    public void setCareRated(boolean careRated) {
        CareRated = careRated;
    }

    public boolean isFamRated() {
        return FamRated;
    }

    public void setFamRated(boolean famRated) {
        FamRated = famRated;
    }

    public double getSalary() {
        return Salary;
    }

    public void setSalary(double salary) {
        Salary = salary;
    }

    public int getHoursWorked() {
        return HoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        HoursWorked = hoursWorked;
    }

    public List<DayAndTime> getShifts() {
        return Shifts;
    }

    public void setShifts(List<DayAndTime> shifts) {
        Shifts = shifts;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (Active ? 1 : 0));
        parcel.writeInt(ShiftsWorked);
        parcel.writeDouble(AccumulatedSalary);
        parcel.writeString(CareUserID);
        parcel.writeString(FamUserID);
        parcel.writeString(Name);
        parcel.writeParcelable(ProfilePicture, i);
        parcel.writeByte((byte) (CareRated ? 1 : 0));
        parcel.writeByte((byte) (FamRated ? 1 : 0));
        parcel.writeDouble(Salary);
        parcel.writeInt(HoursWorked);
        parcel.writeTypedList(Shifts);
    }
}
