package com.earwormfix.earwormfix.Models;

import com.google.gson.annotations.SerializedName;
/**
 * ProfileModel POJO
 * */
public class ProfileModel {
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("full_name")
    private String fullName;
    @SerializedName("uid")
    private String uid;
    @SerializedName("phone")
    private String phone;
    @SerializedName("gender")
    private String gender;
    @SerializedName("birth")
    private String birth;
    @SerializedName("genre")
    private String genre;
    @SerializedName("photo")
    private String photo;
    @SerializedName("created_at")
    private String created;
    @SerializedName("error")
    private boolean error;
    @SerializedName("error_msg")
    private String errMsg;

    public ProfileModel(String name, String email, String fullName,
                        String uid,
                        String phone, String gender, String birth,
                        String genre, String photo, String created, boolean error, String errMsg){
        this.birth =birth;
        this.errMsg = errMsg;
        this.email = email;
        this.fullName = fullName;
        this.name = name;
        this.uid = uid;
        this.phone = phone;
        this.gender =gender;
        this.genre = genre;
        this.photo = phone;
        this.photo = photo;
        this.created = created;
        this.error = error;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public String getCreated() {
        return created;
    }

    public boolean isError() {
        return error;
    }

    public String getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBirth() {
        return birth;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGender() {
        return gender;
    }

    public String getGenre() {
        return genre;
    }

    public String getUid() {
        return uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
