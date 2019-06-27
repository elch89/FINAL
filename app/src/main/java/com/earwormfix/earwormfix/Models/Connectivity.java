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
    public Connectivity(String uid, String name, String full_name, String email, boolean friends, String photo){
        this.email = email;
        this.full_name = full_name;
        this.name = name;
        this.uid = uid;
        this.friends =friends;
        this.photo = photo;
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
