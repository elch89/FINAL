package com.earwormfix.earwormfix.roomDB;

/*import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.earwormfix.earwormfix.Models.Feed;

*//**specify SQL queries and associate them with method calls *//*
@Dao
public interface FeedsDao {
    @Query("SELECT * FROM feed_table WHERE fid = :fid LIMIT 1")
    Feed findFeedById(int fid);

    @Query("SELECT * FROM feed_table WHERE uid = :uid LIMIT 1")
    Feed findFeedByName(String uid);

//    @Insert(onConflict = OnConflictStrategy.REPLACE) // in case ids are the same
//    void insert(Feed feed);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Feed feed);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Feed... feeds);
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Feed feed);

    @Query("DELETE FROM feed_table")
    void deleteAll();

    *//*@Query("SELECT * from feed_table ORDER BY fid ASC")
    LiveData<List<Feed>> getAllFeeds();*//*
    @Query("SELECT * from feed_table ORDER BY fid ASC")
    DataSource.Factory<Integer,Feed> getAllFeeds();




}*/
