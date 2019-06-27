package com.earwormfix.earwormfix.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private TextView logScreen;
    private Button btnRegister;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        logScreen = (TextView)findViewById(R.id.link_to_login);
        inputFullName = (EditText) findViewById(R.id.reg_fullname);
        inputEmail = (EditText) findViewById(R.id.reg_email);
        inputPassword = (EditText) findViewById(R.id.reg_password);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    //registerUser(name, email, password);
                    Intent send = new Intent(getApplicationContext(), SetProfileActivity.class);
                    send.putExtra("name",name);
                    send.putExtra("email",email);
                    send.putExtra("password", password);
                    startActivity(send);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Listener
        logScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Switching to Login Screen/closing register screen
                /*Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);*/
                finish();
            }
        });
    }
}
