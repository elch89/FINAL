package com.earwormfix.earwormfix.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.earwormfix.earwormfix.AppConfig;
import com.earwormfix.earwormfix.AppController;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.earwormfix.earwormfix.helpers.SessionManager;
import com.earwormfix.earwormfix.validtion.FormValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.earwormfix.earwormfix.validtion.FormValidator.getInstance;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static String PACKAGE_NAME;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PERMISSION_ALL = 2;
    // Tag used to cancel the request
    private static final String tag_string_req = "req_login";
    private static FormValidator validator;
    private EditText inputEmail;
    private EditText inputPassword;
    private String email;
    private String password;
    private SessionManager session;
    private SQLiteHandler db;
    private StringRequest strReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // check permissions
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if(!hasPermissions(getApplicationContext(), PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        PACKAGE_NAME = getApplicationContext().getPackageName();
        /*initialise views*/
        TextView regScreen = (TextView) findViewById(R.id.link_to_register);
        Button login = (Button) findViewById(R.id.btnLogin);
        // instance for validating form fields
        validator = getInstance();

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText)findViewById(R.id.password);

        // set listeners for register redirect and feeds redirect
        regScreen.setOnClickListener(this);
        login.setOnClickListener(this);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());
        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, FeedsActivity.class);
            startActivity(intent);
            finish();

        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnLogin)
        {
            email = inputEmail.getText().toString().trim();
            password = inputPassword.getText().toString().trim();
            validateFields();
            // Check for empty data in the form
            if(validator.isEmpty(inputEmail) || validator.isEmpty(inputPassword)){
                // Prompt user to enter credentials
                Toast.makeText(getApplicationContext(),
                        "Please enter the credentials!", Toast.LENGTH_LONG)
                        .show();
            }
            else {
                validateFields();
            }
        }
        if(v.getId() == R.id.link_to_register) {
            // Start activity without closing
            Intent intent_r = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent_r);
            //finish();
        }



    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {



        strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String full_name = user.getString("full_name");
                        String phone = user.getString("phone");
                        String gender = user.getString("gender");
                        String birth = user.getString("birth");
                        String genre = user.getString("genre");
                        String photo = user.getString("photo");
                        String created_at = user.getString("created_at");

                        // Inserting row in users table
                        db.addUser(uid,name, email, full_name, phone, gender, birth, genre,photo, created_at);
                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                FeedsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.getMessage()!=null){
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
                else{
                    Log.e(TAG, "Login Error: Network error" );
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                }

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode ==PERMISSION_ALL ) {

                // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // continue
        }
            else {
                Toast.makeText(this,"יש לאשר גישה לקבצים פנימיים",Toast.LENGTH_LONG).show();
            }
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void validateFields(){

        if(validator.isEmail(inputEmail) && validator.isPassword(inputPassword)) {
            /*if(validator.isValidPassword(password)){

            }*/
            checkLogin(email, password);
            /*else{
                inputPassword.setError("Password is invalid");
            }*/
        }
        else {
            if(!validator.isEmail(inputEmail)) {
                inputEmail.setError("Please enter a valid email address");
            }
            if(!validator.isPassword(inputPassword)){
                inputPassword.setError("Password should be 5-11 characters");
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(strReq != null) {
            AppController.getInstance().cancelPendingRequests(tag_string_req);
        }

    }

}

