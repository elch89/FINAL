package com.earwormfix.earwormfix.Models;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.google.gson.annotations.SerializedName;

/** Creates a 'entity' or object description to save in database
 * A feed has many comments relation*/
public class Post {
    @SerializedName("id")
    private int id;
    @SerializedName("pid")
    private String pid; // post unique id
    @SerializedName("description")
    private String description;
    @SerializedName("url")
    private String url;
    @SerializedName("uid")
    private String uid; // User unique id
    @SerializedName("length")
    private String length;// Time of post
    @SerializedName("fixed")
    private int fixed;
    @SerializedName("created_at")
    private String created_at;
    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("name")
    private String name;
    @SerializedName("photo")
    private String profPic;
    @SerializedName("comments")
    private Comment[] comments;
    @SerializedName("error")
    private boolean error;
    @SerializedName("error_msg")
    private String errMsg;


    public Post(int id, String pid, String description, String url, String uid, String length,
                int fixed, String created_at, String thumbnail,
                String name, Comment[] comments, String profPic, boolean error, String errMsg){
        this.profPic = profPic;
        this.errMsg =errMsg;
        this.error = error;
        this.id = id;
        this.pid = pid;
        this.description = description;
        this.fixed = fixed;
        this.url = url;
        this.uid = uid;
        this.comments = comments;
        this.created_at = created_at;
        this.length = length;
        this.thumbnail = thumbnail;
        this.name = name;
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

    public String getProfPic() {
        return profPic;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLength() {
        return length;
    }

    public Comment[] getComments() {
        return comments;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getDescription() {
        return description;
    }

    public String getPid() {
        return pid;
    }

    public String getUrl() {
        return url;
    }

    public int getFixed() {
        return fixed;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfPic(String profPic) {
        this.profPic = profPic;
    }

    public static final DiffUtil.ItemCallback<Post> CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post feeds, @NonNull Post t1) {
            return feeds.id == t1.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post feeds, @NonNull Post t1) {
            return feeds.equals(t1);
        }
        /*@Override
        public Post getChangePayload(@NonNull Post f1,@NonNull Post f2){
            return (Post) super.getChangePayload(f1,f2);
        }*/
    };
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        Post post = (Post) obj;

        return this.id == post.id ;
    }
}
