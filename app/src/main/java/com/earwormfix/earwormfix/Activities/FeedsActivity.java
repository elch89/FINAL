package com.earwormfix.earwormfix.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.earwormfix.earwormfix.Adapters.FeedsPagerAdapter;
import com.earwormfix.earwormfix.Fragments.FeedFragment;
import com.earwormfix.earwormfix.Fragments.MyFixFragment;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.earwormfix.earwormfix.helpers.SessionManager;

import java.util.Objects;


public class FeedsActivity extends AppCompatActivity {
    private FeedsPagerAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private SQLiteHandler db;
    private SessionManager session;


    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNav;

    private Dialog mContactDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_feeds);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mContactDialog = new Dialog(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNav = findViewById(R.id.nv);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        // Add a drawer layout for menu toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open, R.string.close){
            public void onDrawerClosed(View view){ }
            public void onDrawerOpened(View drawerView){ }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setLogo(R.drawable.ewf_bg_white);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        // set navigation items
        mNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.search:
                        Intent searchFriends = new Intent(FeedsActivity.this,AddFriendsActivity.class);
                        startActivity(searchFriends);
                        finish();
                        break;
                    case R.id.my_profile:
                        Intent userIntent = new Intent(FeedsActivity.this,ProfileActivity.class);
                        startActivity(userIntent);
                        finish();
                        break;
                    case R.id.invite:// Send email to a friend
                        Toast.makeText(FeedsActivity.this, "Invite",Toast.LENGTH_SHORT).show();break;
                    case R.id.contact:
                        contactPopUp();
                        break;
                    case R.id.settings:
                        Toast.makeText(FeedsActivity.this, "Settings",Toast.LENGTH_SHORT).show();break;
                    case R.id.logout:
                        logoutUser();
                        break;
                    default:
                        return true;
                }

                return true;
            }
        });

        // feedPager gives us the swipe effect on both fragments
        adapter = new FeedsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FeedFragment(), "FEEDS");
        adapter.addFragment(new MyFixFragment(), "MY FIX");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

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
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem signItem = menu.findItem(R.id.addFix);
        signItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(signItem);
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()) {
            case R.id.addFix:
                Intent intent = new Intent(this, AddFeed.class);
                startActivity(intent);
                return true;

            /*case R.id.action_favorite:
                // User chose the "message board
                return true;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    private void contactPopUp() {
        TextView txtclose;
        EditText txtContact, txtFrom, txtSubject;
        Button btnContact;

        mContactDialog.setContentView(R.layout.contact_dialog);
        txtContact = mContactDialog.findViewById(R.id.txtMessage);
        txtclose =(TextView) mContactDialog.findViewById(R.id.txt_back);
        txtFrom = mContactDialog.findViewById(R.id.txtFrom);
        txtSubject = mContactDialog.findViewById(R.id.txtSubject);
        btnContact = (Button) mContactDialog.findViewById(R.id.btnOK);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContactDialog.dismiss();
            }
        });
        // feedback is submitted on click
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send the feedback and then close pop up
                String to = "eli032.eb@gmail.com";//change to server email..
                String from = txtFrom.getText().toString().trim();
                String sub = txtSubject.getText().toString().trim();
                String mess = txtContact.getText().toString().trim();
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.setData(Uri.parse("mailto:")); // only email apps should handle this
                mail.putExtra(Intent.EXTRA_EMAIL,new String[]{to});
                mail.putExtra(Intent.EXTRA_SUBJECT, sub);
                mail.putExtra(Intent.EXTRA_TEXT, mess);
                if (mail.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(mail, "Send email via:"));
                }
                mContactDialog.dismiss();
            }
        });
        Objects.requireNonNull(mContactDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mContactDialog.show();
    }
    // Closes drawer layout on back pressed
    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
