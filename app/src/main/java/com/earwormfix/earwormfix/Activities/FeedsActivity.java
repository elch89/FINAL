package com.earwormfix.earwormfix.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        mContactDialog = new Dialog(this);



        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open, R.string.close){
            public void onDrawerClosed(View view){

            }
            public void onDrawerOpened(View drawerView){

            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mNav = findViewById(R.id.nv);
        mNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.search:
                        Toast.makeText(FeedsActivity.this, "Search",Toast.LENGTH_SHORT).show();break;
                    case R.id.my_profile:
                        Toast.makeText(FeedsActivity.this, "My profile",Toast.LENGTH_SHORT).show();
                        Intent userIntent = new Intent(FeedsActivity.this,ProfileActivity.class);
                        startActivity(userIntent);
                        break;
                    case R.id.invite:
                        Toast.makeText(FeedsActivity.this, "Invite",Toast.LENGTH_SHORT).show();break;
                    case R.id.contact:
                        Toast.makeText(FeedsActivity.this, "Contact us",Toast.LENGTH_SHORT).show();
                        ShowPopup();
                        break;
                    case R.id.settings:
                        Toast.makeText(FeedsActivity.this, "Settings",Toast.LENGTH_SHORT).show();break;
                    case R.id.logout:
                       /* Toast.makeText(FeedsActivity.this, "Logout",Toast.LENGTH_SHORT).show();*/
                        logoutUser();
                        break;
                    default:
                        return true;
                }


                return true;

            }
        });

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
        HashMap<String, String> userProfile = db.getProfileDetails();

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
        db.deleteProfiles();

        // Launching the login activity
        Intent intent = new Intent(FeedsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);

    }



    private void ShowPopup() {
        //TODO: Stop video playback in fragments
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
                String mess = txtContact.getText().toString().concat("  From:" + from);
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.putExtra(Intent.EXTRA_EMAIL,new String[]{to});
                mail.putExtra(Intent.EXTRA_SUBJECT, sub);
                mail.putExtra(Intent.EXTRA_TEXT, mess);
                mail.setType("message/rfc822");
                startActivity(Intent.createChooser(mail, "Send email via:"));

                // TODO: resume Video playback in fragments
                mContactDialog.dismiss();
            }
        });
        Objects.requireNonNull(mContactDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mContactDialog.show();
    }


}
