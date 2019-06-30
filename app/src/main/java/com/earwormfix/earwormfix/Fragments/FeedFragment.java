package com.earwormfix.earwormfix.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.earwormfix.earwormfix.Adapters.FeedAdapter;
import com.earwormfix.earwormfix.Adapters.FeedViewModel;
import com.earwormfix.earwormfix.Adapters.SharedViewModel;
import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.Models.MyFix;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.AddPostIntentService;
import com.earwormfix.earwormfix.Rest.RestApi;
import com.earwormfix.earwormfix.Rest.ResultObject;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.Utilitties.NetworkState;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import java.util.HashMap;
import java.util.Objects;

import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import im.ene.toro.widget.Container;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;


public class FeedFragment extends Fragment  implements ItemClickListener {
    private static final String SERVER_PATH = "https://earwormfix.com";
    Container container;
    FeedAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    SQLiteHandler db;
    private SharedViewModel model;
    private String selectedPost;
    private LinearLayout err;
    private Button retry;
    private SwipeRefreshLayout refreshLayout;
    private FeedViewModel mFeedViewModel;
    private Dialog mDialog;

    public FeedFragment() {} // Required empty public constructor

    private TextView subNew;
    @SuppressLint("CutPasteId")
    public void onViewCreated(@NonNull View views, Bundle savedInstanceState) {
        refreshLayout = views.findViewById(R.id.swipe_main);
        container = views.findViewById(R.id.container);
        err = views.findViewById(R.id.net_err);
        retry = views.findViewById(R.id.net_err).findViewById(R.id.retry_network);
        err.setVisibility(View.INVISIBLE);

        //************************************//
        // Initialise comment dialog
        mDialog = new Dialog(Objects.requireNonNull(getActivity()));

        //***********************************//
        // Set layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new FeedAdapter(PlayerSelector.DEFAULT ,getActivity());
      /*  container.setAdapter(adapter);*/
        container.setPlayerSelector(adapter);
        container.setCacheManager(adapter);
        //*****************Sqlite initializer******************************************//
        db = new SQLiteHandler(getActivity());
        //*******The following initializes the video view in a container************************//
        container.setLayoutManager(layoutManager);

        container.setPlayerDispatcher(__ -> 500); // The playback will be delayed 500ms.
        container.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(false, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });
        // Only when you use Container inside a CoordinatorLayout and depends on Behavior.
        ToroUtil.wrapParamBehavior(container, () -> container.onScrollStateChanged(SCROLL_STATE_IDLE));

        //*****************************//
        // Sets up a listener for comments, fixed and add pressed inside view holders
        adapter.setClickListener(this);
        //---------------Observe live data from server(data base)----------------------//
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mFeedViewModel = ViewModelProviders.of(this).get(FeedViewModel.class);
        fetchFeeds();
        retry.setOnClickListener(v -> {
            if(isOnline()){
                mFeedViewModel.refresh();
                err.setVisibility(View.INVISIBLE);
                container.setVisibility(View.VISIBLE);
            }
        });
        refreshLayout.setOnRefreshListener(()->mFeedViewModel.refresh());
        container.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ON_RESUME","observing service");
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(AddPostIntentService.COMPRESS);
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(testReceiver, filter);
        String quote = (String)getActivity().getIntent().getStringExtra("testing");
        Log.d("ON_RESUME"," " +quote);


    }
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).unregisterReceiver(testReceiver);
    }
    // Define the callback for what to do when data is received
    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                boolean resultValue = intent.getBooleanExtra("resultValue",false);
                if(resultValue){
                    Toast.makeText(getContext(),"הפוסט הועלה בהצלחה",Toast.LENGTH_LONG).show();
                    mFeedViewModel.refresh();
                }
                else{
                    Toast.makeText(getContext(),"ארעה שגיאה בהעלאת הפוסט",Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup containerP, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, containerP, false);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser && getView()!=null);
        if (!isVisibleToUser) {
            if(container != null){
                container.setPlayerSelector(null);
            }
        }
        else{
            if(container!= null){
                initContainer();
            }
        }
    }

    // Click listener for comment text view - add a comment
    @Override
    public void onCommentClick(View view, int position) {
        // a popup window for adding a comment is inflated:
        submitComment(view, position);
    }
    private Parcelable recyclerViewState;
    @Override
    public  void  onFixClick(View view, int position){
        selectedPost = adapter.getFeedAt(position).getPid();
        sendFixed(selectedPost);
        view.setClickable(false);
        initContainer();
    }
    // Add to myfix list
    @Override
    public void onSubmitEdit(View view, int position) {
        // add to list
        Log.e("DEBUG", "add to list clicked");
        Post selectedView = adapter.getFeedAt(position);
        addToPlaylist(selectedView);
        MyFix myFix = new MyFix(1,selectedView.getDescription(),selectedView.getUrl(),selectedView.getThumbnail(),false,"");
        model.select(myFix);

    }

    private void submitComment(View v, int position) {
        TextView txtclose;
        EditText txtComment;
        Button btnComment;

        mDialog.setContentView(R.layout.comment_dialog);
        txtComment = mDialog.findViewById(R.id.inp_commemt);
        txtclose =(TextView) mDialog.findViewById(R.id.txtclose);
        btnComment = (Button) mDialog.findViewById(R.id.commenting);
        txtclose.setOnClickListener(v1 -> mDialog.dismiss());

        // comment is submitted on click
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add the comment to server and then close pop up, and invalidate datasource,
                // to refresh page
                selectedPost = adapter.getFeedAt(position).getPid();
                if(selectedPost!=null){
                    // Find the post id of selected post
                    sendComment(txtComment.getText().toString(),selectedPost);
                    initContainer();

                }
                else{// Means feeds list is empty
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Failed to add comment",Toast.LENGTH_LONG).show();
                }

                mDialog.dismiss();
            }
        });
        Objects.requireNonNull(mDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.show();
    }
    public void initContainer(){
        container.setPlayerSelector(adapter);
        container.setCacheManager(adapter);
        container.setLayoutManager(layoutManager);

        container.setPlayerDispatcher(__ -> 500); // The playback will be delayed 500ms.
        container.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(false, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });
        container.setAdapter(adapter);

    }


    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }
    private void sendComment(String inp, String post_id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // get user unique id
        HashMap<String, String> user = db.getUserDetails();
        String userId = user.get("uid");
        RestApi vInterface = retrofit.create(RestApi.class);
        RequestBody uid = createPartFromString(userId);
        RequestBody pid = createPartFromString(post_id);
        RequestBody input = createPartFromString(inp);
        // Map post parameters
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("comment", input);
        map.put("uid", uid);
        map.put("pid", pid);
        // Call restApi
        Call<ResultObject> serverCom = vInterface.sendRequest(map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    if(!Objects.requireNonNull(result).isStat()){
                        Log.e("SEND_FIX", " Send fix error");
                    }
                    Log.i("SEND_COMMENT", " "+ Objects.requireNonNull(result).getSuccess());
                    mFeedViewModel.refresh();
                }
            }
            @Override
            public void onFailure(Call<ResultObject> call, Throwable t) {
                Log.e("SEND_COMMENT",t.getMessage());
            }
        });

    }

    private void sendFixed(String post_id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // get user unique id

        RestApi vInterface = retrofit.create(RestApi.class);

        RequestBody pid = createPartFromString(post_id);

        // Map post parameters
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("fixed", pid);
        // Call restApi
        Call<ResultObject> serverCom = vInterface.sendRequest(map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    Log.i("SEND_FIX", " "+Objects.requireNonNull(result).getSuccess());
                    if(!result.isStat()){
                        Log.e("SEND_FIX", " Send fix error");
                    }
                }
                mFeedViewModel.refresh();
            }

            @Override
            public void onFailure(Call<ResultObject> call, Throwable t) {
                    Log.e("SEND_FIX",t.getMessage());
            }
        });
    }

    private void addToPlaylist(Post post){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        HashMap<String, String> user = db.getUserDetails();
        String userId = user.get("uid");

        RestApi vInterface = retrofit.create(RestApi.class);
        RequestBody uid = createPartFromString(userId);
        RequestBody description = createPartFromString(post.getDescription());
        RequestBody vidUrl = createPartFromString(post.getUrl());
        RequestBody thumbUrl = createPartFromString(post.getThumbnail());

        // Map post parameters
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("uid", uid);
        map.put("desc", description);
        map.put("vid", vidUrl);
        map.put("thumb", thumbUrl);
        // Call restApi
        Call<ResultObject> serverCom = vInterface.sendRequest(map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    Log.i("ADD_LIST", " "+ Objects.requireNonNull(result).getSuccess());
                    Toast.makeText(getContext(),"השיר נוסף בהצלחה לרשימת ההשמעה", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResultObject> call, Throwable t) {
                Log.e("ADD_LIST",t.getMessage());
            }
        });

    }


    private void fetchFeeds(){
        mFeedViewModel.getPagedListLiveData().observe(getViewLifecycleOwner(), feeds -> {
            Log.d("Observe","observing data");
            adapter.submitList(feeds);
            refreshLayout.setRefreshing(false);
        });
        mFeedViewModel.getNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            Log.d("LIVE", "Network State Change   -"+networkState.getMsg());
            // In case there has been a network error show retry and invalidate data source
            if(networkState.getStatus() == NetworkState.Status.FAILED && !isOnline()){
                err.setVisibility(View.VISIBLE);
                container.setVisibility(View.INVISIBLE);
            }
        });
    }
    // Checks if device is connected to network
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


}
