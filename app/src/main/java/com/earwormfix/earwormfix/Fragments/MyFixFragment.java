package com.earwormfix.earwormfix.Fragments;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.earwormfix.earwormfix.Adapters.SharedViewModel;
import com.earwormfix.earwormfix.Models.MyFix;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.FetchFeedApi;
import com.earwormfix.earwormfix.Rest.FetchFeedApiFactory;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.earwormfix.earwormfix.AppController.getAppContext;


public class MyFixFragment extends Fragment implements ItemClickListener {
    private RecyclerView rv;
    private MyFixAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<MyFix> myFixList;
    private Uri uri;
    private ItemClickListener mListener;
    // Playlist variables
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean shouldAutoPlay;
    private Handler mainHandler;
    private Dialog mFullScreenDialog;
    private boolean mExoPlayerFullscreen;
    private ImageView full;
    private View v;

    private DataSource.Factory mediaDataSourceFactory;

    public MyFixFragment() { }
    @Override
    public void onViewCreated(@NonNull View views, Bundle savedInstanceState){
        rv = views.findViewById(R.id.playlist_recycler_view);
        playerView = views.findViewById(R.id.exoplayerview_activity_video);
        mainHandler = new Handler();
        mediaDataSourceFactory = new DefaultDataSourceFactory(Objects.requireNonNull(getActivity()), Util.getUserAgent(getActivity(),null));

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        initLayout();
        getMyFixList();
        rv.setLayoutManager(layoutManager);
        mListener =this;
        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getSelected().observe(this,item-> getMyFixList());
        // Volume control mute/un mute
        ImageView volumeOff =views.findViewById(R.id.exo_volume_off);
        ImageView volumeOn = views.findViewById(R.id.exo_volume_up);
        volumeOn.setVisibility(View.INVISIBLE);
        volumeOff.setOnClickListener(v -> {
            player.setVolume(0f);
            volumeOn.setVisibility(View.VISIBLE);
            volumeOff.setVisibility(View.INVISIBLE);

        });

        volumeOn.setOnClickListener(v -> {
            player.setVolume(0.75f);
            volumeOn.setVisibility(View.INVISIBLE);
            volumeOff.setVisibility(View.VISIBLE);
        });

        full = views.findViewById(R.id.exo_fullscreen_icon);
        v = views;
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
        if(player != null){
            stop();
            release();
        }

    }

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

    private void initializePlayer(int position) {
        playerView.requestFocus();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        playerView.setPlayer(player);

        MediaSource[] mediaSources = new MediaSource[myFixList.size()-position];
        for (int i = 0; i < myFixList.size()-position; i++) {
            mediaSources[i] = buildMediaSource(Uri.parse("https://www.earwormfix.com/"+myFixList.get(position+i).getVidUrl()));
        }

        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources);
        player.prepare(mediaSource);
    }
    private MediaSource buildMediaSource(Uri uri) {
        return /*new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                mainHandler, null)*/new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(Objects.requireNonNull(getActivity()),"video")).
                createMediaSource(uri);
    }

    public void stop(){
        player.stop(true);
    }
    public void release(){
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    private void getMyFixList(){
        SQLiteHandler db;
        db = new SQLiteHandler(getAppContext());
        HashMap<String, String> user = db.getUserDetails();
        FetchFeedApi ffa= FetchFeedApiFactory.create();
        Call<List<MyFix>> data = ffa.getMyPlaylist(user.get("uid"));

        data.enqueue(new Callback<List<MyFix>>() {
            @Override
            public void onResponse(Call<List<MyFix>> call, Response<List<MyFix>> response) {
                if(response.isSuccessful()){
                    List<MyFix> data = response.body();
                    if(data!=null && !data.get(0).isError()){

                        myFixList = new ArrayList<>();
                        myFixList.addAll(data);
                        initLayout();
                    }
                }
                else{
                    Log.e("API CALL", response.message());
                }
            }

            @Override
            public void onFailure(Call<List<MyFix>> call, Throwable t) {
                Log.e("Error",t.getMessage()+call.toString());
            }
        });
    }

    @Override
    public void onCommentClick(View view, int position) { }

    @Override
    public void onFixClick(View view, int position) { }

    @Override
    public void onSubmitEdit(View view, int position) {
        player.stop();
       if(view.isSelected()){
           view.setSelected(false);
       }
       else {
           view.setSelected(true);
           initializePlayer(position);
       }
    }



    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
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
        /*mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_skrink));*/
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }
    private void closeFullscreenDialog() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        ((FrameLayout) v.findViewById(R.id.main_media_frame)).addView(playerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
    }
    private void initFullscreenButton() {

        /*PlaybackControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);*/
        full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }
    private void initLayout(){
        mAdapter = new MyFixAdapter(myFixList,getContext());
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);
        mAdapter.setClickListener(mListener);

    }
}
