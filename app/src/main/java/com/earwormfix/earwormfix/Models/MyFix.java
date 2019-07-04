package com.earwormfix.earwormfix.Models;

import com.google.gson.annotations.SerializedName;
/**
 * MyFix POJO
 * */
public class MyFix {
    @SerializedName("mid")
    private int id;
    @SerializedName("desc")
    private String songName;
    @SerializedName("video")
    private String vidUrl;
    @SerializedName("thumbnail")
    private String imgUrl;
    @SerializedName("error")
    private boolean error;
    @SerializedName("error_msg")
    private String errMsg;

    public MyFix(int id, String songName, String vidUrl, String imgUrl,boolean error,String errMsg){
        this.id = id;
        this.errMsg =errMsg;
        this.error = error;
        this.imgUrl = imgUrl;
        this.songName = songName;
        this.vidUrl = vidUrl;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public boolean isError() {
        return error;
    }

    public int getId() {
        return id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getVidUrl() {
        return vidUrl;
    }

    public String getSongName() {
        return songName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setVidUrl(String vidUrl) {
        this.vidUrl = vidUrl;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
