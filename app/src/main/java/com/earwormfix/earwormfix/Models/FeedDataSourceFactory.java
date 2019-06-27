package com.earwormfix.earwormfix.Models;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;

import com.earwormfix.earwormfix.FeedDataSource;

public class FeedDataSourceFactory extends DataSource.Factory {
    FeedDataSource feedDataSource;
    MutableLiveData<FeedDataSource> mutableLiveData;
    public  FeedDataSourceFactory(){
        mutableLiveData = new MutableLiveData<>();
    }
    @Override
    public DataSource create() {
        feedDataSource = new FeedDataSource();
        mutableLiveData.postValue(feedDataSource);
        return feedDataSource;
    }
    public MutableLiveData<FeedDataSource> getMutableLiveData() {
        return mutableLiveData;
    }
}
