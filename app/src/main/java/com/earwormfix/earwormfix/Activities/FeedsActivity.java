package com.earwormfix.earwormfix.Activities;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
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

import static com.earwormfix.earwormfix.AppConfig.CHANNEL_ID;

/**
 * This is the main activity where most of app is happening
 * fragments are added to show feed list and personal playlist
 * */
public class FeedsActivity extends AppCompatActivity {
    private static final String EMAIL_ADDRESS = "eli032.eb@gmail.com";
    private static final String FEED_TITLE = "FEEDS";
    private static final String MY_FIX_TITLE = "MY FIX";

    private SQLiteHandler db;
    private SessionManager session;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Dialog mContactDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_feeds);

        // for API 26+, initializes notification on device
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            createNotificationChannel();
        }

        // make phone not go to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set up a customized toolbar, replacing default android one
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Set up the display for custom toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setLogo(R.drawable.ewf_bg_white);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView mNav = (NavigationView)findViewById(R.id.nv);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        // Add the drawer layout for menu and set drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open, R.string.close){
            // necessary to override?, can do something in background?
            public void onDrawerClosed(View view){ }
            public void onDrawerOpened(View drawerView){ }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        // init dialog for contact us
        mContactDialog = new Dialog(this);

        // set navigation view items listener
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
                        Intent userIntent = new Intent(FeedsActivity.this, EditProfileActivity.class);
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
        // this part adds fragments to adapter
        FeedsPagerAdapter fpAdapter = new FeedsPagerAdapter(getSupportFragmentManager());
        fpAdapter.addFragment(new FeedFragment(), FEED_TITLE);
        fpAdapter.addFragment(new MyFixFragment(), MY_FIX_TITLE);
        viewPager.setAdapter(fpAdapter);
        tabLayout.setupWithViewPager(viewPager);
        //TODO: use this for optimization
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

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
    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        // adds menu item addFix for uploading a post
        MenuItem signItem = menu.findItem(R.id.addFix);
        signItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(signItem);
            }
        });
        return true;
    }
    /**
     * Set behaviour when toolbar items are pressed
     * e.g drawer toggling when menu pressed and add a post pressed
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        if(item.getItemId() == R.id.addFix) {
            // add a post activity
            Intent intent = new Intent(this, AddPost.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * once contact us is selected inflates a dialog with
     * fields to fill in Email message
     * */
    private void contactPopUp() {

        mContactDialog.setContentView(R.layout.contact_dialog);
        EditText mMessage = (EditText)mContactDialog.findViewById(R.id.txtMessage);
        TextView txtClose =(TextView) mContactDialog.findViewById(R.id.txt_back);
        EditText txtSubject = (EditText)mContactDialog.findViewById(R.id.txtSubject);
        Button btnContact = (Button) mContactDialog.findViewById(R.id.btnOK);
        // Cancels sen email dialog
        txtClose.setOnClickListener(new View.OnClickListener() {
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
                String sub = txtSubject.getText().toString().trim();
                String mess = mMessage.getText().toString().trim();
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.setData(Uri.parse("mailto:")); // only email apps should handle this
                mail.putExtra(Intent.EXTRA_EMAIL,new String[]{EMAIL_ADDRESS});
                mail.putExtra(Intent.EXTRA_SUBJECT, sub);
                mail.putExtra(Intent.EXTRA_TEXT, mess);
                if (mail.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(mail, "Send email via:"));
                }
                mContactDialog.dismiss();
            }
        });
        // Setup background color for dialog
        if(mContactDialog != null){
            if(mContactDialog.getWindow()!= null){
                mContactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            mContactDialog.show();
        }

    }
     /**
      * Closes drawer layout on back pressed
      * */
    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    /**
     *  Create the NotificationChannel, but only on API 26+ because
     *  the NotificationChannel class is new and not in the support library.
     *  It is important to do this here and not in service
     *  because setting up a channel can take time.
     * */
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Earwormfix";
            String description = "notify data stream";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system;
            // the importance can't be change or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
