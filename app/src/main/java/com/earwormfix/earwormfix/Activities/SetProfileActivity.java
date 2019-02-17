package com.earwormfix.earwormfix.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.earwormfix.earwormfix.Adapters.AvatarAdapter;
import com.earwormfix.earwormfix.R;

public class SetProfileActivity extends AppCompatActivity {

    private Button submit;
    // private EditText t1,t2...
    String[] spinnerTitles;
    int[] spinnerImages;
    Spinner mSpinner;
    private boolean isUserInteracting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        mSpinner = findViewById(R.id.spinner_avatar);
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
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submit = findViewById(R.id.sub_btn);
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetProfileActivity.this,FeedsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        isUserInteracting = true;
    }
}
