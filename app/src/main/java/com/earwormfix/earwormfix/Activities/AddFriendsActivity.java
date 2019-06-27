package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.earwormfix.earwormfix.Adapters.AddFriendAdapter;
import com.earwormfix.earwormfix.Models.Connectivity;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.GetProfilesApi;
import com.earwormfix.earwormfix.Rest.RestApi;
import com.earwormfix.earwormfix.Rest.ResultObject;
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

public class AddFriendsActivity extends AppCompatActivity implements ItemClickListener {
    private RecyclerView rv;
    private static AddFriendAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<Connectivity> connectivityArrayList;
    private HashMap<String, String> user;
    private ItemClickListener mListener;
    private int orientation;

    private static final String SERVER_PATH = "https://earwormfix.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        rv = findViewById(R.id.rv_connect);
        orientation =getRequestedOrientation();
        layoutManager = new LinearLayoutManager(this);
        mListener =this;
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // get user unique id
        user = db.getUserDetails();
        // Fetch friends from data base
        searchFriends();
        //if(mAdapter!=null)



    }

    private void searchFriends(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                    .build();
            // get user unique id

        GetProfilesApi vInterface = retrofit.create(GetProfilesApi.class);

            // Call restApi
        Call<List<Connectivity>> serverCom = vInterface.searchProfiles(user.get("uid"));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        serverCom.enqueue(new Callback<List<Connectivity>>() {
            @Override
            public void onResponse(Call<List<Connectivity>> call, Response<List<Connectivity>> response) {
                if(response.isSuccessful()){
                    List<Connectivity> data = response.body();
                    Log.d("DEBUG","inside response-------->"+data);
                    if(data !=null){
                        connectivityArrayList = new ArrayList<>();
                        connectivityArrayList.addAll(data);
                        mAdapter = new AddFriendAdapter(connectivityArrayList, getApplicationContext());
                        rv.setLayoutManager(layoutManager);
                        rv.setAdapter(mAdapter);
                        mAdapter.setClickListener(mListener);
                        setRequestedOrientation(orientation);
                    }
                }
                else{
                    Log.i("SEARCH_FRIENDS","Error in server");
                }
            }

            @Override
            public void onFailure(Call<List<Connectivity>> call, Throwable t) {
                Log.e("ERROR","FAIL "+t.getMessage());
            }
        });
    }

    @Override
    public void onCommentClick(View view, int position) {
        // add friend in data base
        String uid = connectivityArrayList.get(position).getUid();
        addFriend(uid);

    }

    @Override
    public void onFixClick(View view, int position) {
        //
    }

    @Override
    public void onSubmitEdit(View view, int position) {
        //
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
            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    Intent feedInt = new Intent(AddFriendsActivity.this,FeedsActivity.class);
                    startActivity(feedInt);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResultObject> call, Throwable t) {

            }
        });
    }

}
