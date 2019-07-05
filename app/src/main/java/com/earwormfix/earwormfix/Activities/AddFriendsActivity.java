package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.earwormfix.earwormfix.Adapters.AddFriendAdapter;
import com.earwormfix.earwormfix.Models.Connectivity;
import com.earwormfix.earwormfix.Models.ResultObject;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.GetProfilesApi;
import com.earwormfix.earwormfix.Rest.RestApi;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * This activity is for viewing available users using this app
 * and adding them as friends
 * */
public class AddFriendsActivity extends AppCompatActivity implements ItemClickListener {
    private static final String SERVER_PATH = "https://earwormfix.com";
    private static final String TAG = AddFriendsActivity.class.getSimpleName();
    private RecyclerView rv;
    private AddFriendAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Connectivity> connectivityArrayList;
    private HashMap<String, String> user;
    private ItemClickListener mListener;
    private Call<List<Connectivity>> serverCom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        rv = (RecyclerView)findViewById(R.id.rv_connect);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        connectivityArrayList = new ArrayList<>();// empty list on create
        mAdapter = new AddFriendAdapter(connectivityArrayList, getApplicationContext());
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);
        mAdapter.setClickListener(this);
        mListener = this;
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // get user unique id
        user = db.getUserDetails();
        // Fetch friends from data base
        searchFriends();
        // user is done with activity
        Button backToFeed = (Button)findViewById(R.id.btn_close);
        backToFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddFriendsActivity.this,FeedsActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void searchFriends(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                    .build();
        GetProfilesApi vInterface = retrofit.create(GetProfilesApi.class);

        // Call GetProfilesApi
        serverCom = vInterface.searchProfiles(user.get("uid"));

        // retrofit callback
        serverCom.enqueue(new Callback<List<Connectivity>>() {
            @Override
            public void onResponse(@NonNull Call<List<Connectivity>> call, @NonNull Response<List<Connectivity>> response) {
                if(response.isSuccessful()){
                    List<Connectivity> data = response.body();

                    if(data !=null){
                        if(data.get(0).isErr()){
                            Log.e(TAG,"Server error "+ data.get(0).getErrMsg());
                        }
                        else {
                            connectivityArrayList = new ArrayList<>();
                            connectivityArrayList.addAll(data);
                            mAdapter = new AddFriendAdapter(connectivityArrayList, getApplicationContext());
                            rv.setLayoutManager(layoutManager);
                            rv.setAdapter(mAdapter);
                            mAdapter.setClickListener(mListener);
                            Log.i(TAG,"List of profiles fetched");
                        }

                    }
                    else{
                        Log.e(TAG,"List failed to be fetched" );
                    }
                }
                else{
                    Log.e(TAG,"Error in server CODE "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Connectivity>> call, @NonNull Throwable t) {
                Log.e(TAG,"FAIL to call"+t.getMessage());
            }
        });
    }
    // Listener for adding selected from list
    @Override
    public void onCommentClick(View view, int position) { }
    @Override
    public void onFixClick(View view, int position) { }
    @Override
    public void onItemClick(View view, int position) {
        // add friend in data base
        String uid = connectivityArrayList.get(position).getUid();
        addFriend(uid);
    }
    @Override
    public void onDeleteClick(View view, int position){

    }


    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    private void addFriend(String other_id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestApi vInterface = retrofit.create(RestApi.class);
        // creates post parameters for body of request
        RequestBody uid = createPartFromString(user.get("uid"));
        RequestBody oid = createPartFromString(other_id);

        // Map post parameters
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("my_id", uid);
        map.put("other", oid);
        // Call restApi
        Call<ResultObject> serverCom = vInterface.sendRequest(map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(@NonNull Call<ResultObject> call, @NonNull Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    if(result != null) {
                        // refresh list
                        if(result.isStat()){
                            Log.e(TAG, "Error at server " + result.getSuccess());
                        }
                        else {
                            searchFriends();
                            Log.i(TAG, "Added friend - " + result.getSuccess());
                            Toast.makeText(getApplicationContext(), "Friend added", Toast.LENGTH_LONG).show();
                            // make as friend request?
                        }
                    }
                }
                else {
                    Log.e(TAG, "server failed to add friend  Code - "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResultObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Retrofit call failed- "+t.getMessage());
            }
        });
    }
    // handle possible leaks
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(serverCom != null){
            serverCom.cancel();
        }
        Intent i = new Intent(AddFriendsActivity.this,FeedsActivity.class);
        startActivity(i);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serverCom != null){
            serverCom.cancel();
        }
    }

}
