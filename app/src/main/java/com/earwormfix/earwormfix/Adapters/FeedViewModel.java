package com.earwormfix.earwormfix.Adapters;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import com.earwormfix.earwormfix.FeedDataSource;
import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.Models.FeedDataSourceFactory;
import com.earwormfix.earwormfix.Utilitties.NetworkState;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FeedViewModel extends AndroidViewModel {
    FeedDataSourceFactory feedDataSourceFactory;
    //MutableLiveData<FeedDataSource> dataSourceMutableLiveData;
    Executor executor;
    LiveData<PagedList<Post>> pagedListLiveData;
    LiveData<NetworkState> networkState;
    LivePagedListBuilder<Integer, Post> builder;

    public FeedViewModel(@NonNull Application application) {
        super(application);
        feedDataSourceFactory = new FeedDataSourceFactory();
        //dataSourceMutableLiveData = feedDataSourceFactory.getMutableLiveData();
        networkState = Transformations.switchMap(feedDataSourceFactory.getMutableLiveData(),
                (Function<FeedDataSource, LiveData<NetworkState>>) FeedDataSource::getNetworkState);
        PagedList.Config config = (new PagedList.Config.Builder()).
                setEnablePlaceholders(false).
                setInitialLoadSizeHint(5).setPageSize(5).setPrefetchDistance(2).build();
        executor = Executors.newFixedThreadPool(5);//.setNotifyExecutor(executor)
        builder = new LivePagedListBuilder<Integer, Post>(feedDataSourceFactory,config);
        pagedListLiveData = builder.setFetchExecutor(executor).build();
    }
    public LiveData<PagedList<Post>> getPagedListLiveData() {
        return pagedListLiveData;
    }
    public LiveData<NetworkState> getNetworkState() {
        return networkState;
    }
    public void refresh() {
        Objects.requireNonNull(pagedListLiveData.getValue()).getDataSource().invalidate();
    }

}
