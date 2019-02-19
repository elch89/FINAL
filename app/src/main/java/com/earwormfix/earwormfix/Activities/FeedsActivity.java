package com.earwormfix.earwormfix.Activities;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.earwormfix.earwormfix.Adapters.FeedAdapter;
import com.earwormfix.earwormfix.Adapters.FeedViewModel;
import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.helper.SQLiteHandler;
import com.earwormfix.earwormfix.helper.SessionManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import im.ene.toro.widget.Container;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;


public class FeedsActivity extends AppCompatActivity implements ItemClickListener {
    Container container;
    FeedAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    private Toolbar mTopToolbar;
    private FeedViewModel mFeedViewModel;
    private TextView subNew,mPlaylist;
    public static final int NEW_FEED_ACTIVITY_REQUEST_CODE = 1;

    private SQLiteHandler db;
    private SessionManager session;

    private Dialog mDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_feeds);
        subNew = findViewById(R.id.button);
        mPlaylist = findViewById(R.id.player_chosen);
        container = findViewById(R.id.container);
        //************************************//
        // Initialise comment dialog
        mDialog = new Dialog(this);
        //***********************************//
        // Menu bar initializing
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.baseline_touch_app_black_18dp);
        mTopToolbar.setOverflowIcon(drawable);
        //***********************************//
        // Set layout manager
        layoutManager = new LinearLayoutManager(this);

        adapter = new FeedAdapter(PlayerSelector.DEFAULT ,getApplicationContext());
        container.setPlayerSelector(adapter);

        container.setCacheManager(adapter);
        //*******The following initializes the video view in a container************************//
        container.setLayoutManager(layoutManager);
        container.setAdapter(adapter);
        container.setPlayerDispatcher(__ -> 500); // The playback will be delayed 500ms.
        container.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(false, 0.75f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });

        // Only when you use Container inside a CoordinatorLayout and depends on Behavior.
        ToroUtil.wrapParamBehavior(container, () -> container.onScrollStateChanged(SCROLL_STATE_IDLE));
        //*****************************//
        // Sets up a listener for comments pressed inside view holders
        adapter.setClickListener(this);
        //---------------Observe live data from room persistence library(data base)----------------------//
        mFeedViewModel = ViewModelProviders.of(this).get(FeedViewModel.class);
        // Add an observer that observes the LiveData in the ViewModel.
        // To display the current contents of the database,
        mFeedViewModel.getAllFeeds().observe(this, feeds -> {
            // Update the cached copy of the feeds in the adapter.
            adapter.setFeeds(feeds);
        });
        mFeedViewModel.getAllComments().observe(this,comments -> {
            // Update the cached copy of the comments in the adapter.
            adapter.setComments(comments);
        });
        subNew.setOnClickListener(view -> {
            Intent intent = new Intent(FeedsActivity.this, AddFeed.class);
            startActivityForResult(intent, NEW_FEED_ACTIVITY_REQUEST_CODE);
        });

        //************************************************************//
        mPlaylist.setOnClickListener(view -> {
            Intent in1 = new Intent(FeedsActivity.this,PlaylistActivity.class);
            startActivity(in1);
            finish();
        });

        //************************************************************//


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_FEED_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Feed feed = new Feed( getCurrentTime(), data.getStringExtra("uid"), R.drawable.avatar_dog);

            mFeedViewModel.insert(feed);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Empty",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(FeedsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // do actions on selection from menu-- more to be added
        switch (id){
            case R.id.action_profile:
                Intent intent = new Intent(FeedsActivity.this,SetProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                logoutUser();
                break;
            case R.id.action_options:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Click listener for comment text view - add a comment
    @Override
    public void onCommentClick(View view, int position) {
        //////Toast.makeText(this,"item selected at: "+position,Toast.LENGTH_LONG).show();
        // a popup window for adding a comment is inflated:
        ShowPopup(view, position);
    }
    @Override
    public  void  onFixClick(View view, int position){
        mFeedViewModel.getAllFeeds().observe(this, feeds -> {
            if(feeds != null) {
                feeds.get(position).incrementFixed();
                // Update the adapter that only the item there was changed prevent restart of playback.
                adapter.notifyItemChanged(position);
                //adapter.notifyDataSetChanged();
            }
        });
    }

    private int mFid = -1;
    private void ShowPopup(View v, int position) {
        TextView txtclose;
        EditText txtComment;
        Button btnComment;

        mDialog.setContentView(R.layout.comment_dialog);
        txtComment = mDialog.findViewById(R.id.inp_commemt);
        txtclose =(TextView) mDialog.findViewById(R.id.txtclose);
        btnComment = (Button) mDialog.findViewById(R.id.commenting);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        // Get the id of the first feed and add with position to determine new comments id relative to feed
        mFeedViewModel.getAllFeeds().observe(this, feeds -> {
            if(feeds != null)
                mFid = feeds.get(0).getId();
        });
        // comment is submitted on click
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add the comment to room and then close pop up
                // Position + feed id --> is the feed that was chosen to comment about
                if(mFid != -1){
                    Comment comment = new Comment((position + mFid), txtComment.getText().toString(),getCurrentTime());
                    mFeedViewModel.insertComment(comment);
                }
                else{// Means feeds list is empty
                    Toast.makeText(getApplicationContext(),"Failed to add comment",Toast.LENGTH_LONG).show();
                }

                mDialog.dismiss();
            }
        });
        Objects.requireNonNull(mDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.show();
    }
    /** Get the current time for comment and feed added*/
    private String getCurrentTime() {
        long millis = new Date().getTime();
        return DateUtils.formatDateTime( getApplicationContext(),millis, DateUtils.FORMAT_SHOW_TIME);
    }
}
