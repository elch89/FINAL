package com.earwormfix.earwormfix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.earwormfix.earwormfix.Adapters.PlaylistAdapter;


public class MyFixFragment extends Fragment {
    private RecyclerView rv;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public MyFixFragment() { }
    @Override
    public void onViewCreated(@NonNull View views, Bundle savedInstanceState){
        rv = views.findViewById(R.id.playlist_recycler_view);
        //playerView = findViewById(R.id.single_player);
        String[] myDataset ={"Song 1", "Song 2","Song 3","Song 4","Song 5","Song 6"};

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PlaylistAdapter(myDataset);
        rv.setAdapter(mAdapter);

    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_fix, container, false);
    }

}
