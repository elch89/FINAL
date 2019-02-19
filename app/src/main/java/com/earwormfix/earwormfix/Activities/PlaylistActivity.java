package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.earwormfix.earwormfix.Adapters.PlaylistAdapter;
import com.earwormfix.earwormfix.R;

public class PlaylistActivity extends AppCompatActivity {

    private RecyclerView rv;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView toFeeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        rv = (RecyclerView) findViewById(R.id.playlist_recycler_view);
        //playerView = findViewById(R.id.single_player);
        String[] myDataset ={"Song 1", "Song 2","Song 3","Song 4","Song 5","Song 6"};

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PlaylistAdapter(myDataset);
        rv.setAdapter(mAdapter);

        toFeeds = findViewById(R.id.feeds_chosen_playlist);
        toFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Switching to Login Screen/closing register screen
                Intent i = new Intent(getApplicationContext(),
                        FeedsActivity.class);
                startActivity(i);
                finish();
            }
        });

    }



}
