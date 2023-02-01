package com.example.proyectoceti.ClassesAndModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.List;

public class UserModel implements Parcelable {
    private float Rating;
    private int RatingQuantity;
    private HashMap<String, Object> UserInfo = null;
    private String UserType;
    private String Expertise;
    private String FCMToken;
    private boolean Banned;
    private List<String> VinculatedFam = null;
    private List<String> VinculatedCare = null;

    public UserModel() {
    }

    public UserModel( float rating, int ratingQuantity, HashMap<String, Object> userInfo, String userType, String expertise, String FCMToken, boolean Banned, String cellPhoneNumber, List<String> vinculatedFam, List<String> vinculatedCare) {
        Rating = rating;
        RatingQuantity = ratingQuantity;
        UserInfo = userInfo;
        UserType = userType;
        Expertise = expertise;
        this.FCMToken = FCMToken;
        this.Banned = Banned;
        VinculatedFam = vinculatedFam;
        VinculatedCare = vinculatedCare;
    }


    protected UserModel(Parcel in) {
        Rating = in.readFloat();
        RatingQuantity = in.readInt();
        UserInfo = (HashMap<String, Object>) in.readSerializable();
        UserType = in.readString();
        Expertise = in.readString();
        FCMToken = in.readString();
        Banned = in.readByte() != 0;
        VinculatedFam = in.createStringArrayList();
        VinculatedCare = in.createStringArrayList();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    @PropertyName("Rating")
    public float getRating() {
        return Rating;
    }

    @PropertyName("Rating")
    public void setRating(float rating) {
        Rating = rating;
    }

    @PropertyName("RatingQuantity")
    public int getRatingQuantity() {
        return RatingQuantity;
    }

    @PropertyName("RatingQuantity")
    public void setRatingQuantity(int ratingQuantity) {
        RatingQuantity = ratingQuantity;
    }

    @PropertyName("UserInfo")
    public HashMap<String, Object> getUserInfo() {
        return UserInfo;
    }

    @PropertyName("UserInfo")
    public void setUserInfo(HashMap<String, Object> userInfo) {
        UserInfo = userInfo;
    }

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public String getExpertise() {
        return Expertise;
    }

    public void setExpertise(String expertise) {
        Expertise = expertise;
    }

    @PropertyName("FCM Token")
    public String getFCMToken() {
        return FCMToken;
    }

    @PropertyName("FCM Token")
    public void setFCMToken(String FCMToken) {
        this.FCMToken = FCMToken;
    }

    @PropertyName("Banned")
    public boolean getBanned() {
        return Banned;
    }

    @PropertyName("Banned")
    public void setBanned(boolean banned) {
        Banned = banned;
    }

    public List<String> getVinculatedFam() {
        return VinculatedFam;
    }

    public void setVinculatedFam(List<String> vinculatedFam) {
        VinculatedFam = vinculatedFam;
    }

    public List<String> getVinculatedCare() {return VinculatedCare;}

    public void setVinculatedCare(List<String> vinculatedCare) {VinculatedCare = vinculatedCare;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(Rating);
        parcel.writeInt(RatingQuantity);
        parcel.writeSerializable(UserInfo);
        parcel.writeString(UserType);
        parcel.writeString(Expertise);
        parcel.writeString(FCMToken);
        parcel.writeByte((byte) (Banned ? 1 : 0));
        parcel.writeStringList(VinculatedFam);
        parcel.writeStringList(VinculatedCare);
    }
}
