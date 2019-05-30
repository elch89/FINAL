package com.earwormfix.earwormfix.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.earwormfix.earwormfix.Adapters.AvatarAdapter;
import com.earwormfix.earwormfix.AppConfig;
import com.earwormfix.earwormfix.AppController;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.helper.SQLiteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SetProfileActivity extends AppCompatActivity {

    private Button submit;
    String[] spinnerTitles;
    int[] spinnerImages;
    Spinner mSpinner;
    private boolean isUserInteracting;

    // input selected:
    private String sAvatar;
    private int sAvatarImg;
    private EditText mFullName;
    private EditText mEmail;
    private EditText mPhone;
    private RadioGroup mGender;
    private EditText mBirth;
    private EditText mGenre;

    private ProgressDialog pDialog;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        // Init views
        mSpinner = findViewById(R.id.spinner_avatar);
        mFullName = findViewById(R.id.full_name_edit);
        mEmail = findViewById(R.id.email_edit);
        mPhone = findViewById(R.id.phone_edit);
        mGender = findViewById(R.id.sex_select);
        mBirth = findViewById(R.id.bday_edit);
        mGenre = findViewById(R.id.ganre_edit);


        spinnerTitles = new String[]{"Dog", "Girl", "Woman", "Child", "Man"};
        spinnerImages = new int[]{R.drawable.avatar_dog
                , R.drawable.avatar_girl
                , R.drawable.avatar_woman
                , R.drawable.child
                , R.drawable.man
                };

        AvatarAdapter mCustomAdapter = new AvatarAdapter(SetProfileActivity.this, spinnerTitles, spinnerImages);
        mSpinner.setAdapter(mCustomAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isUserInteracting) {
                    Toast.makeText(SetProfileActivity.this, spinnerTitles[i], Toast.LENGTH_SHORT).show();
                    sAvatar = spinnerTitles[i];
                    sAvatarImg = spinnerImages[i];
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                sAvatar = spinnerTitles[0];
                sAvatarImg = spinnerImages[0];
            }
        });

        submit = findViewById(R.id.sub_btn);
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = mFullName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                int sGender = mGender.getCheckedRadioButtonId();
                RadioButton btnGender = findViewById(sGender);
                String gender = btnGender.getText().toString().trim();
                String birth = mBirth.getText().toString().trim();
                String genre = mGenre.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                             "Please enter your details!", Toast.LENGTH_LONG)
                             .show();
                    registerUserProfile(name, email, phone, gender, birth, genre,  String.valueOf(sAvatarImg));
                }
                else {
                    Toast.makeText(getApplicationContext(),
                                   "Please enter your details!", Toast.LENGTH_LONG)
                                    .show();
                }
            }
        });
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
    }
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        isUserInteracting = true;
    }
    /**
     * Function to store user in MySQL database will post params to register url
     * */
    private void registerUserProfile(final String name, final String email,
                              final String phone, final String gender, final String birth,
                              final String genre, final String avatar) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PROFILE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("profile");
                        String full_name = user.getString("full_name");
                        String email = user.getString("email");
                        String phone = user.getString("phone");
                        String gender = user.getString("gender");
                        String birth = user.getString("birth");
                        String genre = user.getString("genre");
                        String avatar = user.getString("avatar");


                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addProfile(full_name, email, phone, gender, birth, genre, avatar, uid, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity - Only if register successful
                        Intent intent = new Intent(
                                SetProfileActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("full_name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("gender", gender);
                params.put("birth", birth);
                params.put("genre", genre);
                params.put("avatar", avatar);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
