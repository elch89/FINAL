package com.earwormfix.earwormfix.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.earwormfix.earwormfix.Adapters.EditProfileAdapter;
import com.earwormfix.earwormfix.AppConfig;
import com.earwormfix.earwormfix.AppController;
import com.earwormfix.earwormfix.Models.ResultObject;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.DeleteApi;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.earwormfix.earwormfix.helpers.SessionManager;
import com.earwormfix.earwormfix.validtion.FormValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileActivity extends AppCompatActivity implements ItemClickListener, DatePicker.OnDateChangedListener, View.OnClickListener {
    private static final String SERVER_PATH = "https://earwormfix.com";
    private static final String TAG = EditProfileActivity.class.getSimpleName();
    // Tag used to cancel the request
    private static final String tag_string_req = "req_update";
    private RecyclerView rv;
    private EditText txtEdit;
    private Dialog mDialog;
    private SQLiteHandler db;
    public ProgressBar progressBar;
    private String[] mDataset;
    private String[] mCurrent;
    private String[] mKeyNames;
    private SessionManager session;
    private StringRequest strReq;
    // constants for keys
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTH = "birth";
    private static final String KEY_GENRE = "genre";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        // Init views
        rv = findViewById(R.id.profile_recycler_view);
        progressBar = findViewById(R.id.progress_update);
        Button backToFeed = findViewById(R.id.btn_back_to_feeds);
        session = new SessionManager(getApplicationContext());
        // Initialise update dialog
        mDialog = new Dialog(this);
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        getProfileFromMySql();// Display details in data set
        mKeyNames = new String[]{ KEY_FULL_NAME, KEY_EMAIL,KEY_BIRTH, KEY_GENDER, KEY_GENRE, KEY_PHONE};
        backToFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btn_back_to_feeds){
                    Intent backInt = new Intent(EditProfileActivity.this, FeedsActivity.class);
                    startActivity(backInt);
                    /* Go back to FeedsActivity*/
                    finish();
                }
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent backInt = new Intent(EditProfileActivity.this, FeedsActivity.class);
        startActivity(backInt);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onCommentClick(View view, int position) {}
    @Override
    public void onFixClick(View view, int position) {}
    @Override
    public void onItemClick(View view, int position) {
        updateAt(view, position);// change information in database
    }
    @Override
    public void onDeleteClick(View view, int position){

    }

    private void updateAt(View v, int position) {

        mDialog.setContentView(R.layout.update_dialog);
        txtEdit = mDialog.findViewById(R.id.txt_edit);
        TextView txtClose = mDialog.findViewById(R.id.txt_cancel);
        Button btnSubmit = mDialog.findViewById(R.id.btn_submit);
        // in case of birth date edit
        if(KEY_BIRTH.equals(mKeyNames[position])){
            txtEdit.setInputType(InputType.TYPE_NULL);
            txtEdit.setFocusable(false);
            txtEdit.setClickable(true);
            pickerDialog();
        }

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        // update is submitted on click
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // update databases
                String col = mKeyNames[position];
                HashMap<String, String> userProfile = db.getUserDetails();
                String email = userProfile.get("email");
                String param = txtEdit.getText().toString().trim();
                if (isValid(position) && email != null ) {
                    Toast.makeText(getApplicationContext(),
                            "Updating...", Toast.LENGTH_LONG)
                            .show();
                    progressBar.setVisibility(View.VISIBLE);
                    updateUserProfile(col, email, param);
                    mDialog.dismiss();
                }
            }
        });
        Objects.requireNonNull(mDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.show();
    }

    private void updateUserProfile(final String col, final String email, final String param) {

        strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);

                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully edited in MySQL
                        // Now update the user in sqlite

                        JSONObject toUpdate = jObj.getJSONObject("profile");
                        String email = toUpdate.getString("email");
                        String col = toUpdate.getString("col");
                        String par = toUpdate.getString("param");
                        // Updating row in user table
                        db.updateProfile(col, email, par);
                        getProfileFromMySql();// refresh profile details on recycler view
                        Toast.makeText(getApplicationContext(), "Profile successfully updated", Toast.LENGTH_LONG).show();
                    } else {
                        // Error occurred in update. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                        Log.d("error_msg", errorMsg);
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                Log.d("error message", "ERROR "+error.getMessage());

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to update url
                Map<String, String> params = new HashMap<String, String>();
                params.put("col", col);
                params.put("email", email);
                params.put("param", param);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    /**
     * Fetch profile details from mysql database
     * Set adapter to view details
     * (on submit edit or on create)
     */
    private void getProfileFromMySql(){
        HashMap<String, String> userProfile = db.getUserDetails();

        mCurrent = new String[]{userProfile.get(KEY_FULL_NAME), userProfile.get(KEY_EMAIL), userProfile.get(KEY_BIRTH),
                          userProfile.get(KEY_GENDER), userProfile.get(KEY_GENRE), String.valueOf(userProfile.get(KEY_PHONE))};

        mDataset = new String[]{"NAME: ", "EMAIL: ", "BIRTH DATE: ", "GENDER: ", "GENRE: ", "PHONE: "};
        EditProfileAdapter mAdapter = new EditProfileAdapter(mDataset, mCurrent);
        // Update layout for new data
        rv.setAdapter(mAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter.setClickListener(this);

    }

    private void pickerDialog(){
        final Dialog dtPickerDlg = new Dialog(getApplicationContext());
        dtPickerDlg.setContentView(R.layout.picker);
        txtEdit.setInputType(InputType.TYPE_NULL);
        txtEdit.setClickable(true);
        txtEdit.setFocusable(false);
        txtEdit.setOnClickListener(v -> {
            final DatePicker picker =(DatePicker) dtPickerDlg.findViewById(R.id.datePicker);
            Button btnOk =(Button) dtPickerDlg.findViewById(R.id.okbutton);
            btnOk.setOnClickListener(v1 -> dtPickerDlg.dismiss());
            Calendar c = Calendar.getInstance();
            int dd = c.get(Calendar.DAY_OF_MONTH);
            int mm = c.get(Calendar.MONTH);
            int yy = c.get(Calendar.YEAR);
            picker.init(yy,mm,dd,this);//last parameter is datechangelistener
            dtPickerDlg.show();
            dtPickerDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }
            });
        });


    }
    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year,monthOfYear,dayOfMonth);
        String inp = DateFormat.format("dd/MM/yyyy",c).toString();
        txtEdit.setText(inp);
    }

    public boolean isValid(int position){
        FormValidator fv = FormValidator.getInstance();
        if(fv.isEmpty(txtEdit)){
            Toast.makeText(getApplicationContext(),
                    "Field is empty!", Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        if(KEY_EMAIL.equals(mKeyNames[position])){
            if(!fv.isEmail(txtEdit)){
                Toast.makeText(getApplicationContext(),
                        "Please enter valid email!", Toast.LENGTH_LONG)
                        .show();
                return false;
            }
        }
        else if(KEY_PHONE.equals(mKeyNames[position])){
            if(!fv.isValidPhone(txtEdit.getText().toString().trim())){
                Toast.makeText(getApplicationContext(),
                        "Please enter a valid phone number!", Toast.LENGTH_LONG)
                        .show();
                return false;
            }
        }
        return true;
    }
    @Override
    public void onClick(View v){
        if(v.getId() == R.id.link_remove){
            // Construct a new Alert Dialog box
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
            // Add the buttons, positive/negetive
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    deleteUser();
                    logoutUser();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                // User cancelled the dialog
                dialog.dismiss();
            });
            // Set message to display
            builder.setMessage(R.string.dialog_delete)
                    .setTitle(R.string.dialog_title);
            // Create dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }
    public void deleteUser(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        HashMap<String, String> user = db.getUserDetails();
        String userId = user.get("uid");

        DeleteApi vInterface = retrofit.create(DeleteApi.class);
        // Map post parameters
        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody uid = createPartFromString(userId);
        map.put("uid", uid);
        // Call restApi
        Call<ResultObject> serverCom = vInterface.deleteItem(map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(@NonNull Call<ResultObject> call, @NonNull retrofit2.Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    Log.i(TAG, " message from server - "+ Objects.requireNonNull(result).getSuccess());
                    Toast.makeText(getApplicationContext(),"הפעולה בוצעה בהצלחה", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResultObject> call, @NonNull Throwable t) {
                Log.e(TAG,"Error in callback" + t.getMessage());
            }
        });
    }
    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        // Launching the login activity
        Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(strReq != null) {
            AppController.getInstance().cancelPendingRequests(tag_string_req);
        }

    }
}
