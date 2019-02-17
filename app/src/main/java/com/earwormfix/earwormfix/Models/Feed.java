package com.earwormfix.earwormfix.Models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/** Creates a 'entity' or object description to save in database
 * A feed has many comments relation*/
@Entity(tableName = "feed_table",
        indices = {@Index(value = "uid", unique = true)})
public class Feed  {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "fid")
    private int id; // table unique id
    @NonNull
    @ColumnInfo(name = "video")
    private String vidId;
    @NonNull
    @ColumnInfo(name = "top")
    private String top;// Time of post
    @NonNull
    @ColumnInfo(name = "uid")
    private String uid; // User unique id
    @ColumnInfo(name = "fixed")
    private int fixed;
    @ColumnInfo(name = "profile_pic")
    private int profile_pic;

/** @Ignore for adding fields that will not be added to table*/
    public Feed(@NonNull String top,@NonNull String uid, int profile_pic){

        this.profile_pic = profile_pic;
        this.vidId = "0";
        this.fixed = 0;
        this.top = top;
        this.uid = uid;
    }


    public int getId() {
        return id;
    }

    public int getFixed() {
        return fixed;
    }

    @NonNull
    public String getTop() {
        return top;
    }
    @NonNull
    public String getUid() {
        return uid;
    }
    @NonNull
    public String getVidId() {
        return vidId;
    }

    public int getProfile_pic() {
        return profile_pic;
    }


    public void setId(int myId) {
        this.id = myId;
    }

    public void setTop(@NonNull String top) {
        this.top = top;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    public void setVidId(@NonNull String vidId) {
        this.vidId = vidId;
    }

    public void incrementFixed(){
        this.fixed += 1;
    }

    public void setProfile_pic(int profile_pic) {
        this.profile_pic = profile_pic;
    }
}
