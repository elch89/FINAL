package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.earwormfix.earwormfix.R;
/** An activity to add feeds */
public class AddFeed extends AppCompatActivity {


    private EditText mEditUid;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds_add);
        mEditUid = findViewById(R.id.edit_uid);


        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                //if (TextUtils.isEmpty(mEditWordView.getText())) {
                 //   setResult(RESULT_CANCELED, replyIntent);
                //} else {

                String word3 = mEditUid.getText().toString();

                replyIntent.putExtra("uid", word3);


                setResult(RESULT_OK, replyIntent);
                //}
                finish();
            }
        });
    }
}
