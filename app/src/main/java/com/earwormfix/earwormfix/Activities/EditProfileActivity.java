package com.earwormfix.earwormfix.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.earwormfix.earwormfix.Adapters.EditProfileAdapter;
import com.earwormfix.earwormfix.AppConfig;
import com.earwormfix.earwormfix.AppController;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity implements ItemClickListener, DatePicker.OnDateChangedListener {
    private RecyclerView rv;
    private EditProfileAdapter mAdapter;
    private EditText txtEdit;
    private Button backToFeed;
    private Button mEdit;
    private Dialog mDialog;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    String[] mDataset;
    String[] mCurrent;
    String[] mKeyNames;

    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTH = "birth";
    private static final String KEY_GENRE = "genre";
    /*private static final String KEY_PHOTO = "photo";*/
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // Init views
        rv = findViewById(R.id.profile_recycler_view);
        backToFeed = findViewById(R.id.btn_back_to_feeds);

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
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }
    @Override
    public void onBackPressed() {
        Intent backInt = new Intent(EditProfileActivity.this, FeedsActivity.class);
        startActivity(backInt);
        super.onBackPressed();
    }

    @Override
    public void onCommentClick(View view, int position) {
        // do nothing
    }

    @Override
    public void onFixClick(View view, int position) {
        // do nothing
    }

    @Override
    public void onSubmitEdit(View view, int position) {

        ShowPopup(view, position);// change information in database
    }

    private void ShowPopup(View v, int position) {

        mDialog.setContentView(R.layout.update_dialog);
        txtEdit = mDialog.findViewById(R.id.txt_edit);
        TextView txtClose = mDialog.findViewById(R.id.txt_cancel);
        Button btnSubmit = mDialog.findViewById(R.id.btn_submit);

        if(KEY_BIRTH.equals(mKeyNames[position])){
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

                if (!col.isEmpty() && email != null) {
                    Toast.makeText(getApplicationContext(),
                            "Updating...", Toast.LENGTH_LONG)
                            .show();
                    updateUserProfile(col, email, param);

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter text!", Toast.LENGTH_LONG)
                            .show();
                }

                mDialog.dismiss();
            }
        });
        Objects.requireNonNull(mDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.show();
    }

    private void updateUserProfile(final String col, final String email, final String param) {
        // Tag used to cancel the request
        String tag_string_req = "req_update";

        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
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
                        String updated_at = toUpdate.getString("updated_at");
                        // Updating row in user table
                        if(col.equals(KEY_EMAIL)){

                        }
                        db.updateProfile(col, email, par, updated_at);

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
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideDialog();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("error message", "ERROR "+error.getMessage());
                hideDialog();
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
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
        mAdapter = new EditProfileAdapter(mDataset, mCurrent);
        // Update layout for new data
        rv.setAdapter(mAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.setClickListener(this);

    }

    private void pickerDialog(){
        final Dialog dtPickerDlg = new Dialog(this);
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
}
