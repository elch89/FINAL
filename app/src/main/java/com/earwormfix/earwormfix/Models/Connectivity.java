package com.earwormfix.earwormfix.Models;

import com.google.gson.annotations.SerializedName;

public class Connectivity {
    // uid,name,full_name,email
    @SerializedName("uid")
    private String uid;
    @SerializedName("name")
    private String name;
    @SerializedName("full_name")
    private String full_name;
    @SerializedName("email")
    private String email;
    @SerializedName("are_friends")
    private boolean friends;
    @SerializedName("photo")
    private String photo;
    @SerializedName("error")
    private boolean err;
    @SerializedName("error_msg")
    private String errMsg;
    public Connectivity(String uid, String name, String full_name, String email, boolean friends,
                        String photo, boolean err, String errMsg){
        this.email = email;
        this.full_name = full_name;
        this.name = name;
        this.uid = uid;
        this.friends =friends;
        this.photo = photo;
        this.err =err;
        this.errMsg =errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public boolean isErr() {
        return err;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getName() {
        return name;
    }

    public boolean isFriends() {
        return friends;
    }

    public void setFriends(boolean friends) {
        this.friends = friends;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public void setErr(boolean err) {
        this.err = err;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
