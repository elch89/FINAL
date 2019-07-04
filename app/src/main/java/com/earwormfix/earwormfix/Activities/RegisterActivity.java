package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.validtion.FormValidator;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText inputNickName;
    private EditText inputEmail;
    private EditText inputPassword;
    private static FormValidator ins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView logScreen = (TextView)findViewById(R.id.link_to_login);
        inputNickName = (EditText) findViewById(R.id.reg_fullname);
        inputEmail = (EditText) findViewById(R.id.reg_email);
        inputPassword = (EditText) findViewById(R.id.reg_password);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        ins = FormValidator.getInstance();
        // Register or go back Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                validateInput();
            }
        });
        logScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void validateInput(){
        if(ins.isEmpty(inputEmail) || ins.isEmpty(inputPassword) || ins.isEmpty(inputNickName)){
            Toast.makeText(getApplicationContext(),
                    "Please enter all details!", Toast.LENGTH_LONG)
                    .show();
        }
        else {
            if(ins.isEmail(inputEmail) && ins.isPassword(inputPassword) && ins.validateName(inputNickName)) {
                if(ins.isValidPassword(inputPassword.getText().toString().trim())){
                    // proceed
                    String name = inputNickName.getText().toString().trim();
                    String email = inputEmail.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();
                    Intent send = new Intent(getApplicationContext(), SetProfileActivity.class);
                    send.putExtra("name",name);
                    send.putExtra("email",email);
                    send.putExtra("password", password);
                    startActivity(send);
                    finish();
                }
                else{
                    inputPassword.setError("Password must contain - upper case letter, or " +
                            "lower case letters or digits, and no whitespace characters");
                }
            }
            else {
                if(!ins.isEmail(inputEmail)) {
                    inputEmail.setError("Please enter a valid email address");
                }
                if(!ins.isPassword(inputPassword)){
                    inputPassword.setError("Password must be 5-11 characters long");
                }
                if(!ins.validateName(inputNickName)){
                    inputNickName.setError("Name must contain digits and letters only");
                }
            }

        }

    }
}
