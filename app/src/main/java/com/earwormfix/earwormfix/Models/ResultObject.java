package com.earwormfix.earwormfix.Models;

import com.google.gson.annotations.SerializedName;
/**
 * ResultObject POJO for general post calls
 * */
public class ResultObject {
    @SerializedName("error")
    private boolean stat;
    @SerializedName("error_msg")
    private String success;
    public ResultObject(String success, boolean stat) {
        this.success = success;
        this.stat = stat;
    }

    public boolean isStat() {
        return stat;
    }

    public String getSuccess() {
        return success;
    }
}
