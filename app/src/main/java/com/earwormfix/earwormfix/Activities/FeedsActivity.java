package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.earwormfix.earwormfix.Adapters.FeedsPagerAdapter;
import com.earwormfix.earwormfix.FeedFragment;
import com.earwormfix.earwormfix.MyFixFragment;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.helper.SQLiteHandler;
import com.earwormfix.earwormfix.helper.SessionManager;

import java.util.HashMap;
import java.util.Objects;


public class FeedsActivity extends AppCompatActivity {
    private FeedsPagerAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar mTopToolbar;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_feeds);
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(mTopToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.baseline_touch_app_black_18dp);
        mTopToolbar.setOverflowIcon(drawable);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new FeedsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FeedFragment(), "FEEDS");
        adapter.addFragment(new MyFixFragment(), "MY FIX");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


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
}
