package com.earwormfix.earwormfix.service;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.Rest.FetchFeedApi;
import com.earwormfix.earwormfix.factory.FetchFeedApiFactory;
import com.earwormfix.earwormfix.Utilitties.NetworkState;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.earwormfix.earwormfix.AppController.getAppContext;
/**
 * A data source for pages content - Retrofit calls are instantiated to get information from server.
 * extends from PageKeyedDataSource :
 * Incremental data loader for page-keyed content, where requests return keys for next/previous pages.
 *
 * is kind of a service, because preforms calls in background
 * */
public class FeedDataSource extends PageKeyedDataSource<Integer, Post> {
    private static final String TAG = FeedDataSource.class.getSimpleName();
    private SQLiteHandler db;
    private HashMap<String, String> user;
    private FetchFeedApi ffa;
    private MutableLiveData networkState;
    private MutableLiveData initialLoading;

    public FeedDataSource(){
        db = new SQLiteHandler(getAppContext());
        networkState = new MutableLiveData();
        initialLoading = new MutableLiveData();
        this.ffa = FetchFeedApiFactory.create();
        user = db.getUserDetails();
    }
    // for getting state of network calls
    public MutableLiveData getNetworkState() {
        return networkState;
    }
    public MutableLiveData getInitialLoading() {
        return initialLoading;
    }
    // Loads first page on create of activity/fragment
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Post> callback) {
        Log.i(TAG, "Loading Rang " + 1 + " Count " + params.requestedLoadSize);
        // inform mutable data that page is loading
        initialLoading.postValue(NetworkState.LOADING);
        networkState.postValue(NetworkState.LOADING);
        // create a call from retrofit interface class
        Call<List<Post>> data = ffa.fetchPosts(1, params.requestedLoadSize,user.get("uid"));
        // Start call
        data.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.isSuccessful()){
                    List<Post> postList = response.body();
                    if(postList !=null ){
                        if(!postList.get(0).isError()){
                            // POJO is returned based on json response from web service
                            // put returned value in callback(if we got what we wanted)
                            callback.onResult(postList,null,  2);
                            initialLoading.postValue(NetworkState.LOADED);
                            networkState.postValue(NetworkState.LOADED);
                        }
                        else {
                            Log.i(TAG, "Error with returned list, message- "+postList.get(0).getErrMsg());
                        }
                    }
                    else {
                        Log.i(TAG, "Post list is empty");
                    }

                }
                else{
                    // Response from server failed -
                    Log.e(TAG, response.message()+ "  Code- "+response.code());
                    initialLoading.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }

            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                // call failed
                Log.e(TAG,t.getMessage()+call.toString());
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, t.getMessage()));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Post> callback) {
        // Do nothing
    }
    // next pages callback
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Post> callback) {
        Log.i(TAG, "Loading Rang " + params.key + " Count " + params.requestedLoadSize);
        networkState.postValue(NetworkState.LOADING);
        Call<List<Post>> data = ffa.fetchPosts( params.key,params.requestedLoadSize,user.get("uid"));
        data.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.isSuccessful()){
                    List<Post> postList = response.body();
                    if(postList !=null){
                        if(!postList.get(0).isError()){
                            networkState.postValue(NetworkState.LOADED);
                            callback.onResult(postList,params.key+1);
                        }
                        else {
                            Log.i(TAG, "Error with returned list, message- "+postList.get(0).getErrMsg());
                        }
                    }
                    else {
                        Log.i(TAG, "Post list is empty");
                    }
                }
                else {
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    Log.e(TAG, response.message()+ "  Code- "+response.code());
                }

            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.d(TAG,t.getMessage());
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, t.getMessage()));
            }
        });
    }

}
