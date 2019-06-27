package com.earwormfix.earwormfix.Models;

import com.google.gson.annotations.SerializedName;

/**A comment can have only one feed relation*/

public class Comment {
    @SerializedName("pid")
    private String pid; // post unique id
    @SerializedName("uid")
    private String uid;//unique id in table
    @SerializedName("user_input")// The comment
    private String user_input;
    @SerializedName("created_at")// Time of comment
    private String created_at;


    public Comment(String pid, String uid, String user_input, String created_at) {
        this.uid = uid;
        this.user_input = user_input;
        this.created_at = created_at;
        this.pid = pid;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getPid() {
        return pid;
    }

    public String getUid() {
        return uid;
    }

    public String getUser_input() {
        return user_input;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUser_input(String user_input) {
        this.user_input = user_input;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
