package com.earwormfix.earwormfix.factory;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;

import com.earwormfix.earwormfix.service.FeedDataSource;

public class FeedDataSourceFactory extends DataSource.Factory {
    private MutableLiveData<FeedDataSource> mutableLiveData;
    public  FeedDataSourceFactory(){
        mutableLiveData = new MutableLiveData<>();
    }
    @Override
    public DataSource create() {
        FeedDataSource feedDataSource = new FeedDataSource();
        mutableLiveData.postValue(feedDataSource);
        return feedDataSource;
    }
    public MutableLiveData<FeedDataSource> getMutableLiveData() {
        return mutableLiveData;
    }
}
