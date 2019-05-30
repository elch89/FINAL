package com.earwormfix.earwormfix.roomDB;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.Models.Videos;
import com.earwormfix.earwormfix.Rest.RestApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
                            .addCallback(sRoomDatabaseCallback(context))// Callback created below
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    /**To delete all content and repopulate the database whenever the app is started - creates a callback*/
    private static RoomDatabase.Callback sRoomDatabaseCallback(final Context context) {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                /*Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        getDatabase(context).feedsDao().insertAll(loadJSON());
                    }
                });*/
                /*getDatabase(context).feedsDao().insertAll(loadJSON());*/
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
                new PopulateDbAsync(INSTANCE).execute();
            }
        };
    }
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
            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    loadJSON();
                    mDao.insertAll(feedList);
                }
                });
            // fetch videos from database here
            /*Feed feed1 = new Feed("21:12","user#1", R.drawable.avatar_dog);
            final  int multiple = (int)mDao.insert(feed1);
            Comment comment = new Comment("UserA", multiple,"hey you","3:14");
            Comment comment2 = new Comment("UserB", multiple,"Whats up","11:15");

            mcDao.insertComment(comment,comment2);*/
            return null;
        }

    }
    private static ArrayList<Videos> data;
    private static Feed[] feedList;
    private static void loadJSON() {
        /**/
        feedList = new Feed[30];
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://earwormfix.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi request = retrofit.create(RestApi.class);
        Call<String> call = request.fetchVideo();
        call.enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                String jsonResponse = response.body();
                Log.d("Error",jsonResponse);
                try {
                    JSONObject jObj = new JSONObject(jsonResponse);
                    Log.d("Error","--------------"+jsonResponse);

                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONArray strea = jObj.getJSONArray("stream");
                        for(int i= 0 ; i<strea.length();i++){
                            JSONObject dataobj = strea.getJSONObject(i);
                            String uid = dataobj.getString("uid");
                            String location = dataobj.getString("location");
                            String length = dataobj.getString("length");
                            String createdAt = dataobj.getString("created_at");

                        }
                    }

                }
                catch (JSONException e){e.printStackTrace();}
                /*for(int i=0; i<30||i<data.size();i++) {
                    Videos curr = data.get(i);
                    feedList[i] = new Feed(curr.getCreated(), curr.getvUid(), R.drawable.avatar_dog);
                    feedList[i].setVidUri(curr.getLocation());

                    //final int multiple = (int) mDao.insert(mFeed);
                }*/

                    /*adapter = new DataAdapter(data);
                recyclerView.setAdapter(adapter);*/
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });

    }


}
