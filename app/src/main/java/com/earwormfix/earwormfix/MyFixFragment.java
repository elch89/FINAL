package com.earwormfix.earwormfix;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.earwormfix.earwormfix.Adapters.MyFixAdapter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.Objects;


public class MyFixFragment extends Fragment {
    private RecyclerView rv;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private PlayerView playerView;
    private SimpleExoPlayer player;

    public MyFixFragment() { }
    @Override
    public void onViewCreated(@NonNull View views, Bundle savedInstanceState){
        rv = views.findViewById(R.id.playlist_recycler_view);
        playerView = views.findViewById(R.id.exoplayerview_activity_video);
        String[] myDataset ={"Song 1", "Song 2","Song 3","Song 4","Song 5","Song 6"};

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyFixAdapter(myDataset);
        rv.setAdapter(mAdapter);

        //initializePlayer();

    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_fix, container, false);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stop();
        release();
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
            initializePlayer();
        }
    }


    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                getActivity(),
                new DefaultTrackSelector());

        playerView.setPlayer(player);

        player.setPlayWhenReady(false);
        player.seekTo(0, 0);

        start();
    }
    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(Objects.requireNonNull(getActivity()),"video")).
                createMediaSource(uri);
    }
    public void start(){
        Uri uri = Uri.parse("file:///android_asset/video/video.mp4");;
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);
    }

    public void stop(){
        player.stop(true);
    }
    public void release(){
        player.release();

    }

}
