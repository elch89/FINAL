package com.earwormfix.earwormfix.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Videos implements Serializable {
    @SerializedName("uid")
    private String vUid;
    @SerializedName("location")
    private String location;
    @SerializedName("length")
    private String length;
    @SerializedName("created_at")
    private String created;
    public Videos(String vUid, String location, String length, String created){
        this.created = created;
        this.length = length;
        this.location = location;
        this.vUid = vUid;
    }

    public String getCreated() {
        return created;
    }

    public String getLength() {
        return length;
    }

    public String getLocation() {
        return location;
    }

    public String getvUid() {
        return vUid;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setvUid(String vUid) {
        this.vUid = vUid;
    }
}
