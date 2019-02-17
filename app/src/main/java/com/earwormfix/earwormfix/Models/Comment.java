package com.earwormfix.earwormfix.Models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**A comment can have only one feed relation*/
@Entity(tableName = "comment_table",
        foreignKeys = @ForeignKey(entity = Feed.class,
                parentColumns = "fid",
                childColumns = "feedId",
                onDelete = ForeignKey.CASCADE),
        indices = { @Index("feedId"), @Index("TOC")})
public class Comment {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cid")
    private int id;//unique id in table
    /* User that comment was posted by*/
    @ColumnInfo(name = "feedId")// Identification of related feed
    private int feedId;
    @NonNull
    @ColumnInfo(name = "Comment")// The comment
    private String comment;
    @NonNull
    @ColumnInfo(name = "TOC")// Time of comment
    private String toc;


    public Comment(int feedId, @NonNull String comment, @NonNull String toc){
        this.comment = comment;
        this.feedId = feedId;
        this.toc = toc;
    }

    public int getId(){
        return id;
    }
    @NonNull
    public String getToc() {
        return toc;
    }
    @NonNull
    public String getComment() {
        return comment;
    }
    public int getFeedId() {
        return feedId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setToc(@NonNull String toc) {
        this.toc = toc;
    }

    public void setComment(@NonNull String comment) {
        this.comment = comment;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }
}
