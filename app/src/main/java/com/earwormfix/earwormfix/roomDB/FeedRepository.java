/*
package com.earwormfix.earwormfix.roomDB;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.AsyncTask;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Feed;
*/
/**handles data operations. It provides a clean API to the rest of the app for app data
 * A Repository manages query threads and allows you to use multiple backends.
 * In the most common example, the Repository implements the logic for deciding whether
 * to fetch data from a network or use results cached in a local database*//*

public class FeedRepository {
    private FeedsDao mFeedDao;
    private CommentsDao mCommentsDao;
    */
/*private LiveData<List<Comment>> mAllComments;
    private LiveData<List<Feed>> mAllFeeds;*//*

    private LiveData<PagedList<Comment>> mAllComments;
    private LiveData<PagedList<Feed>> mAllFeeds;
    //private   LiveData<List<String>> mAllUid;

    public FeedRepository(Application application) {
        FeedRoomDatabase db = FeedRoomDatabase.getDatabase(application);
        mFeedDao = db.feedsDao();
        mCommentsDao = db.commentsDao();
        mAllComments = new LivePagedListBuilder<>(mCommentsDao.getAllComments(), 30).build();
        mAllFeeds = new LivePagedListBuilder<>(mFeedDao.getAllFeeds(), 30).build();
    }

    */
/*public  LiveData<List<Feed>> getAllFeeds() {
        return mAllFeeds;
    }
    public  LiveData<List<Comment>> getAllComments() {
        return mAllComments;
    }*//*

    public  LiveData<PagedList<Feed>> getAllFeeds() {
        return mAllFeeds;
    }
    public  LiveData<PagedList<Comment>> getAllComments() {
        return mAllComments;
    }
    //public  LiveData<List<String>> getCommentsUid(){
     //   return mAllUid;
    //}

    public void insert (Feed feed) {
        new insertAsyncTask(mFeedDao).execute(feed);
    }
    public void insertComment (Comment comment) {
        new insertCommentAsyncTask(mCommentsDao).execute(comment);
    }


    private static class insertAsyncTask extends AsyncTask<Feed, Void, Void> {

        private FeedsDao mAsyncTaskDao;

        insertAsyncTask(FeedsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Feed... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
    private static class insertCommentAsyncTask extends AsyncTask<Comment, Void, Void> {

        private CommentsDao mAsyncTaskDao;

        insertCommentAsyncTask(CommentsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Comment... params) {
            mAsyncTaskDao.insertComment(params[0]);
            return null;
        }
    }
}
*/
