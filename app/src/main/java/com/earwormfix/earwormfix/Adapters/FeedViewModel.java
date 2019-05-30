package com.earwormfix.earwormfix.Adapters;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.PagedList;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.roomDB.FeedRepository;

/**The ViewModel's role is to provide data to the UI and survive configuration changes.
 * A ViewModel acts as a communication center between the Repository and the UI.
 * You can also use a ViewModel to share data between fragments
 * For both comments and feeds!!*/
public class FeedViewModel extends AndroidViewModel {
    /*private FeedRepository mRepository;
    ////

    private LiveData<List<Feed>> mAllFeeds;
    /////
    private LiveData<List<Comment>> mAllComments;
    //private LiveData<List<String>> mCommentsUid;

    public FeedViewModel (Application application) {
        super(application);
        mRepository = new FeedRepository(application);
        mAllFeeds = mRepository.getAllFeeds();
        /////
        mAllComments = mRepository.getAllComments();
        //mCommentsUid = mRepository.getCommentsUid();
    }

    public LiveData<List<Feed>> getAllFeeds() { return mAllFeeds; }

    public void insert(Feed feed) { mRepository.insert(feed); }
    //----Comments part-----//
    public LiveData<List<Comment>> getAllComments() { return mAllComments; }
    //public LiveData<List<String>> getCommentsUid() { return mCommentsUid;}
    public void insertComment(Comment comment) { mRepository.insertComment(comment); }*/

/*************************************PAGING**********************************************/

    private FeedRepository mRepository;

    public final LiveData<PagedList<Feed>> mAllFeeds;
    public final LiveData<PagedList<Comment>> mAllComments;

    public void insert(Feed feed) { mRepository.insert(feed); }
    public void insertComment(Comment comment) { mRepository.insertComment(comment); }


    public FeedViewModel (Application application) {
        super(application);
        mRepository = new FeedRepository(application);
        mAllFeeds = mRepository.getAllFeeds();
        mAllComments = mRepository.getAllComments();

    }
    public LiveData<PagedList<Feed>> getAllFeeds() { return mAllFeeds; }
    public LiveData<PagedList<Comment>> getAllComments() { return mAllComments; }


}
