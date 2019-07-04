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
import com.earwormfix.earwormfix.Rest.DeleteApi;
import com.earwormfix.earwormfix.viewModels.FeedViewModel;
import com.earwormfix.earwormfix.viewModels.SharedViewModel;
import com.earwormfix.earwormfix.Models.MyFix;
import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.service.AddPostIntentService;
import com.earwormfix.earwormfix.Rest.RestApi;
import com.earwormfix.earwormfix.Models.ResultObject;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.Utilitties.NetworkState;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import java.util.HashMap;
import java.util.Objects;

import im.ene.toro.PlayerSelector;
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
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;


public class FeedFragment extends Fragment  implements ItemClickListener {
    private static final String SERVER_PATH = "https://earwormfix.com";
    private static final String TAG = FeedFragment.class.getSimpleName();
    private Container container;
    private FeedAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SQLiteHandler db;
    private SharedViewModel model;
    private String selectedPost;
    private LinearLayout err;
    private SwipeRefreshLayout refreshLayout;
    private FeedViewModel mFeedViewModel;
    private Dialog mDialog;
    private HashMap<String, String> user;

    public FeedFragment() {} // Required empty public constructor
    @SuppressLint("CutPasteId")
    public void onViewCreated(@NonNull View views, Bundle savedInstanceState) {
        refreshLayout = views.findViewById(R.id.swipe_main);
        container = views.findViewById(R.id.container);
        err = views.findViewById(R.id.net_err);
        Button retry = views.findViewById(R.id.net_err).findViewById(R.id.retry_network);
        err.setVisibility(View.INVISIBLE);
        // Initialise comment dialog
        mDialog = new Dialog(Objects.requireNonNull(getActivity()));
        // Set layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new FeedAdapter(PlayerSelector.DEFAULT ,getActivity());
        container.setPlayerSelector(adapter);
        container.setCacheManager(adapter);
        db = new SQLiteHandler(getActivity());
        container.setLayoutManager(layoutManager);
        user = db.getUserDetails();
        container.setPlayerDispatcher(__ -> 500); // The playback will be delayed 500ms.
        // volume is initialized as off on creation
        container.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(false, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });
        // Sets up a listener for comments, fixed and add pressed inside view holders
        adapter.setClickListener(this);
        // set view models for observers
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mFeedViewModel = ViewModelProviders.of(this).get(FeedViewModel.class);
        fetchFeeds();
        // retry button in case network not available on device
        retry.setOnClickListener(v -> {
            if(isOnline()){
                mFeedViewModel.refresh();
                initContainer();
                err.setVisibility(View.INVISIBLE);
                container.setVisibility(View.VISIBLE);
            }
        });
        refreshLayout.setOnRefreshListener(()-> {
            mFeedViewModel.refresh();
            initContainer();
        });
        container.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG,"observing service");
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(AddPostIntentService.COMPRESS);
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(testReceiver, filter);
    }
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).unregisterReceiver(testReceiver);
    }
    /**Define the callback for what to do when data is received*/
    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                boolean resultValue = intent.getBooleanExtra("resultValue",false);
                if(resultValue){
                    Toast.makeText(getContext(),"הפוסט הועלה בהצלחה",Toast.LENGTH_LONG).show();
                    mFeedViewModel.refresh();
                    initContainer();
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
    /**
     * fragment visibility setup
     * */
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
    @Override
    public  void  onFixClick(View view, int position){
        String[] params = {adapter.getFeedAt(position).getPid()};
        retrofitPost(params);
    }
    // Add to myfix list
    @Override
    public void onItemClick(View view, int position) {
        // add to list
        Post post = adapter.getFeedAt(position);
        String[] params ={post.getDescription(), post.getUrl(), post.getThumbnail()};
        retrofitPost(params);
        MyFix myFix = new MyFix(1,post.getDescription(),post.getUrl(),post.getThumbnail(),false,"");
        model.select(myFix);

    }
    @Override
    public void onDeleteClick(View view, int position){
        String userId = user.get("uid");
        Post post = adapter.getFeedAt(position);
        String[] params ={"0",post.getPid(),post.getUrl(),post.getThumbnail()};
        if(post.getUid().equals(userId)){
            retrofitPost(params);
        }

    }


    private void submitComment(View v, int position) {
        mDialog.setContentView(R.layout.comment_dialog);
        EditText txtComment = mDialog.findViewById(R.id.inp_commemt);
        TextView txtClose =(TextView) mDialog.findViewById(R.id.txtclose);
        Button btnComment = (Button) mDialog.findViewById(R.id.commenting);
        txtClose.setOnClickListener(v1 -> mDialog.dismiss());
        // comment is submitted on click
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add the comment to server and then close pop up, and invalidate data source
                selectedPost = adapter.getFeedAt(position).getPid();
                if(selectedPost!=null){
                    // Find the post id of selected post
                    String[] params = {adapter.getFeedAt(position).getPid(), txtComment.getText().toString()};
                    retrofitPost(params);
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
    /**
     * initialize container after refresh
     * */
    public void initContainer(){
        container.setPlayerSelector(adapter);
        container.setCacheManager(adapter);
        container.setLayoutManager(layoutManager);

        container.setPlayerDispatcher(__ -> 500);
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

    private void fetchFeeds(){
        mFeedViewModel.getPagedListLiveData().observe(getViewLifecycleOwner(), feeds -> {
            Log.i(TAG,"observing data");
            adapter.submitList(feeds);
            refreshLayout.setRefreshing(false);
        });
        mFeedViewModel.getNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            Log.i(TAG, "Network State Change   -"+ Objects.requireNonNull(networkState).getMsg());
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

    private void retrofitPost(String ...params){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        String userId = user.get("uid");

        DeleteApi dIn = null;
        RestApi vInterface = null;
        if(params.length == 4){
            dIn  = retrofit.create(DeleteApi.class);
        }
        else {
           vInterface = retrofit.create(RestApi.class);
        }

        // Map post parameters
        HashMap<String, RequestBody> map = new HashMap<>();
        switch (params.length){

            case 1:
                //send fixed
                    RequestBody pid = createPartFromString(params[0]);
                    map.put("fixed", pid);
                    break;

            case 2:// comment

                    RequestBody uid1 = createPartFromString(userId);
                    RequestBody pid1 = createPartFromString(params[0]);
                    RequestBody input = createPartFromString(params[1]);
                    map.put("comment", input);
                    map.put("uid", uid1);
                    map.put("pid", pid1);
                    break;

            case 3:
                // add to list
                    RequestBody uid = createPartFromString(userId);
                    RequestBody description = createPartFromString(params[0]);
                    RequestBody vidUrl = createPartFromString(params[1]);
                    RequestBody thumbUrl = createPartFromString(params[2]);
                    map.put("uid", uid);
                    map.put("desc", description);
                    map.put("vid", vidUrl);
                    map.put("thumb", thumbUrl);
                    break;
            case 4: // delete post
                RequestBody pidd = createPartFromString(params[1]);
                RequestBody vidUrld = createPartFromString(params[2]);
                RequestBody thumbUrld = createPartFromString(params[3]);
                map.put("pid", pidd);
                map.put("vid", vidUrld);
                map.put("pic", thumbUrld);

                break;

                default:
                    // not enough params
                    return;

        }
        Call<ResultObject> serverCom;
        // Call restApi
        if(params.length == 4){
            serverCom = dIn.deleteItem(map);
        }
        else {
            serverCom = vInterface.sendRequest(map);
        }


        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(@NonNull Call<ResultObject> call, @NonNull Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    Log.i(TAG, " message from server - "+ Objects.requireNonNull(result).getSuccess());
                    Toast.makeText(getContext(),"הפעולה בוצעה בהצלחה", Toast.LENGTH_LONG).show();
                    mFeedViewModel.refresh();
                    initContainer();
                }


            }
            @Override
            public void onFailure(@NonNull Call<ResultObject> call, @NonNull Throwable t) {
                Log.e(TAG,"Error in callback" + t.getMessage());
            }
        });
    }
}
