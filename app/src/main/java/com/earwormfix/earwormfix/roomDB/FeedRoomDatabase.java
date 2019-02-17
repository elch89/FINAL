package com.earwormfix.earwormfix.roomDB;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.R;

/** Add the room database*/
@Database(entities = {Feed.class, Comment.class}, version = 4)
public abstract class FeedRoomDatabase extends RoomDatabase {

    public abstract FeedsDao feedsDao();
    public abstract CommentsDao commentsDao();

    private static volatile FeedRoomDatabase INSTANCE;

    static FeedRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FeedRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FeedRoomDatabase.class, "feed_database")
                            .fallbackToDestructiveMigration()//.allowMainThreadQueries()
                            .addCallback(sRoomDatabaseCallback)// Callback created below
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    /**To delete all content and repopulate the database whenever the app is started - creates a callback*/
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };
    /**Delete all tables*/
    public void clearDb() {
        if (INSTANCE != null) {
            new PopulateDbAsync(INSTANCE).execute();
        }
    }
    /**Here is the code for the AsyncTask that deletes the contents of the database -
     * relevant for populating it on create*/
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final FeedsDao mDao;
        private final CommentsDao mcDao;

        PopulateDbAsync(FeedRoomDatabase db) {
            mDao = db.feedsDao();
            mcDao = db.commentsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();
            mcDao.deleteAllComment();

            Feed feed1 = new Feed("21:12","user#1", R.drawable.avatar_dog);
            final  int multiple = (int)mDao.insert(feed1);
            Comment comment = new Comment(multiple,"hey you","3:14");
            Comment comment2 = new Comment(multiple,"Whats up","11:15");

            mcDao.insertComment(comment,comment2);
            return null;
        }
    }
}
