package com.earwormfix.earwormfix.Fragments;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.earwormfix.earwormfix.Adapters.MyFixAdapter;
import com.earwormfix.earwormfix.viewModels.SharedViewModel;
import com.earwormfix.earwormfix.Models.MyFix;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.FetchFeedApi;
import com.earwormfix.earwormfix.factory.FetchFeedApiFactory;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.earwormfix.earwormfix.AppController.getAppContext;


public class MyFixFragment extends Fragment implements ItemClickListener {
    private static final String TAG = MyFixFragment.class.getSimpleName();
    private static final String URL = "https://www.earwormfix.com/";
    private RecyclerView rv;
    private Call<List<MyFix>> data;
    private RecyclerView.LayoutManager layoutManager;
    private List<MyFix> myFixList;
    private ItemClickListener mListener;
    // Playlist variables
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private Dialog mFullScreenDialog;
    private boolean mExoPlayerFullscreen;
    private ImageView full;
    private View views;
    private ImageView volumeOn;
    private ImageView volumeOff;


    public MyFixFragment() { }
    @Override
    public void onViewCreated(@NonNull View views, Bundle savedInstanceState){
        rv = views.findViewById(R.id.playlist_recycler_view);
        playerView = views.findViewById(R.id.exoplayerview_activity_video);
        volumeOff =views.findViewById(R.id.exo_volume_off);
        volumeOn = views.findViewById(R.id.exo_volume_up);
        full = views.findViewById(R.id.exo_fullscreen_icon);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        mListener = this;
        // Setup adapter
        initLayout();
        // fetch list from server
        getMyFixList();

        SharedViewModel model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SharedViewModel.class);
        // observing live data changes when user adds a video from posts and loads data from server
        model.getSelected().observe(this, myFix -> MyFixFragment.this.getMyFixList());
        // Volume control mute/un mute
        volumeOn.setVisibility(View.INVISIBLE);
        volumeUp();
        volumeDown();
        this.views = views;
        mExoPlayerFullscreen = false;
        initFullscreenButton();
        initFullscreenDialog();

    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_fix, container, false);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(data != null){
            data.cancel();
        }
        if(player != null){
            stop();
            release();
        }
    }
    /**
     * fragment visibility settings
     * */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser && getView()!=null);
        if (!isVisibleToUser) {
            if(player != null){
                stop();
                release();
            }
        }else {
            if(myFixList !=null)
                initializePlayer(0);
        }
    }
    /**
     * @param position selected video position to start playing
     * */
    private void initializePlayer(int position) {
        playerView.requestFocus();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        playerView.setPlayer(player);
        // sets up a playlist
        MediaSource[] mediaSources = new MediaSource[myFixList.size()-position];
        for (int i = 0; i < myFixList.size()-position; i++) {
            mediaSources[i] = buildMediaSource(Uri.parse(URL+myFixList.get(position+i).getVidUrl()));
        }
        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources);
        player.prepare(mediaSource);
    }
    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(Objects.requireNonNull(getActivity()),"video")).
                createMediaSource(uri);
    }

    public void stop(){
        player.stop(true);
    }
    public void release(){
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }
    /** gets the list from data base,
     * list contains - added from feed and uploaded by connected user
     * */
    public void getMyFixList(){
        SQLiteHandler db;
        db = new SQLiteHandler(getAppContext());
        HashMap<String, String> user = db.getUserDetails();
        FetchFeedApi ffa= FetchFeedApiFactory.create();
        data = ffa.getMyPlaylist(user.get("uid"));
        data.enqueue(new Callback<List<MyFix>>() {
            @Override
            public void onResponse(@NonNull Call<List<MyFix>> call, @NonNull Response<List<MyFix>> response) {
                if(response.isSuccessful()){
                    List<MyFix> data = response.body();
                    if(data!=null && !data.get(0).isError()){
                        myFixList = data;
                        initLayout();
                    }
                }
                else{
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MyFix>> call, @NonNull Throwable t) {
                Log.e(TAG,t.getMessage()+call.toString());
            }
        });
    }

    @Override
    public void onCommentClick(View view, int position) { }

    @Override
    public void onFixClick(View view, int position) { }

    @Override
    public void onItemClick(View view, int position) {
        player.stop();
       if(view.isSelected()){
           view.setSelected(false);
       }
       else {
           view.setSelected(true);
           initializePlayer(position);
       }
    }
    @Override
    public void onDeleteClick(View v,int pos){}

     /**
      * Allow full screen playback
      * */
    private void initFullscreenDialog() {
        mFullScreenDialog = new Dialog(Objects.requireNonNull(getActivity()), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }
    private void openFullscreenDialog() {
        ((ViewGroup) playerView.getParent()).removeView(playerView);
        mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }
    private void closeFullscreenDialog() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        ((FrameLayout) views.findViewById(R.id.main_media_frame)).addView(playerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
    }
    private void initFullscreenButton() {
        full.setOnClickListener(v -> {
            if (!mExoPlayerFullscreen)
                openFullscreenDialog();
            else
                closeFullscreenDialog();
        });
    }
    /**
     * Refresh the adapter and layout for data changes
     * */
    private void initLayout(){
        MyFixAdapter mAdapter;
        mAdapter = new MyFixAdapter(myFixList,getContext());
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);
        mAdapter.setClickListener(mListener);
    }

    /**
     * Set up volume configurations
     * */
    private void volumeUp(){
        volumeOn.setOnClickListener(v -> {
            if(player!=null) {
                player.setVolume(0.75f);
                volumeOn.setVisibility(View.INVISIBLE);
                volumeOff.setVisibility(View.VISIBLE);
            }
        });
    }
    private void volumeDown(){
        volumeOff.setOnClickListener(v -> {
            if(player!=null){
                player.setVolume(0f);
                volumeOn.setVisibility(View.VISIBLE);
                volumeOff.setVisibility(View.INVISIBLE);
            }
        });
    }
}
