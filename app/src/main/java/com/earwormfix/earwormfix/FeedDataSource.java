package com.earwormfix.earwormfix;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.Rest.FetchFeedApi;
import com.earwormfix.earwormfix.Rest.FetchFeedApiFactory;
import com.earwormfix.earwormfix.Utilitties.NetworkState;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.earwormfix.earwormfix.AppController.getAppContext;

public class FeedDataSource extends PageKeyedDataSource<Integer, Feed> {
    private SQLiteHandler db;
    HashMap<String, String> user;
    FetchFeedApi ffa;
    private MutableLiveData networkState;
    private MutableLiveData initialLoading;
    public FeedDataSource(){
        db = new SQLiteHandler(getAppContext());
        networkState = new MutableLiveData();
        initialLoading = new MutableLiveData();
        this.ffa = FetchFeedApiFactory.create();
        user = db.getUserDetails();
    }
    public MutableLiveData getNetworkState() {
        return networkState;
    }
    public MutableLiveData getInitialLoading() {
        return initialLoading;
    }
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Feed> callback) {
        //ffa = RetrofitInstance.getRetrofitInstance().create(FetchFeedApi.class);
        Log.i("DEBUG", "Loading Rang " + 1 + " Count " + params.requestedLoadSize);
        initialLoading.postValue(NetworkState.LOADING);
        networkState.postValue(NetworkState.LOADING);
        //ffa = FetchFeedApiFactory.create();
        Call<List<Feed>> data = ffa.fetchPosts(1, params.requestedLoadSize,user.get("uid"));
        data.enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                if(response.isSuccessful()){
                    List<Feed> feedList = response.body();
                    if(feedList!=null && !feedList.get(0).isError()){
                        callback.onResult(feedList,null,  2);
                        initialLoading.postValue(NetworkState.LOADED);
                        networkState.postValue(NetworkState.LOADED);
                    }
                }
                else{
                    Log.e("API CALL", response.message());
                    initialLoading.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }

            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Log.e("Error",t.getMessage()+call.toString());
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, t.getMessage()));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Feed> callback) {
        // Do nothing
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Feed> callback) {
        Log.i("DEBUG", "Loading Rang " + params.key + " Count " + params.requestedLoadSize);
        networkState.postValue(NetworkState.LOADING);
        //ffa = RetrofitInstance.getRetrofitInstance().create(FetchFeedApi.class);
        Call<List<Feed>> data = ffa.fetchPosts( params.key,params.requestedLoadSize,user.get("uid"));
        data.enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                if(response.isSuccessful()){
                    List<Feed> feedList = response.body();
                    if(feedList!=null && !feedList.get(0).isError()){
                        networkState.postValue(NetworkState.LOADED);
                        callback.onResult(feedList,params.key+1);
                    }
                }
                else {
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    Log.e("API CALL", response.message());
                }

            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Log.d("Error",t.getMessage());
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, t.getMessage()));
            }
        });
    }

}
