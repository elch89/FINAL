/*
package com.earwormfix.earwormfix.roomDB;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.earwormfix.earwormfix.Models.Comment;

import java.util.List;

@Dao
public interface CommentsDao {
    @Query("SELECT * from comment_table ORDER BY TOC DESC")
    DataSource.Factory<Integer, Comment> getAllComments();
    */
/*@Query("SELECT * from comment_table ORDER BY TOC DESC")
    LiveData<List<Comment>> getAllComments();*//*


    @Query("SELECT feedId FROM comment_table")
    LiveData<List<Integer>> getAllFeedIds();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComment(Comment... comment);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void update(Comment comment);

    @Query("DELETE FROM comment_table")
    void deleteAllComment();
}
*/
