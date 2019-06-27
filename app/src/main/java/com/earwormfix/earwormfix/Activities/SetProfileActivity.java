package com.earwormfix.earwormfix.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.earwormfix.earwormfix.Models.ProfileModel;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.RegisterApi;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SetProfileActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener,EasyPermissions.PermissionCallbacks {
    private static final int READ_REQUEST_CODE = 200;
    private static final int SELECT_IMAGE = 1;
    private static final String SERVER_PATH = "https://earwormfix.com";
    private static final String TAG = SetProfileActivity.class.getSimpleName();
    private Button submit,upload;
    private ImageView imgAvatar;
    private String pathToImage;
    private Uri uri;
    //String[] spinnerTitles;
    //int[] spinnerImages;
    //Spinner mSpinner;
    /*private boolean isUserInteracting;*/

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
    private File imgFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        // Init views
        //mSpinner = findViewById(R.id.spinner_avatar);
        mFullName = findViewById(R.id.full_name_edit);
        mEmail = findViewById(R.id.email_edit);
        mPhone = findViewById(R.id.phone_edit);
        mGender = findViewById(R.id.sex_select);
        mBirth = findViewById(R.id.bday_edit);//picker
        mGenre = findViewById(R.id.ganre_edit);
        submit = findViewById(R.id.sub_btn);

        upload = findViewById(R.id.upload_pic);
        imgAvatar = findViewById(R.id.selectedPhoto);
        imgAvatar.setImageResource(R.drawable.avatar_dog);
        imgAvatar.setTag(R.drawable.avatar_dog);
        // init dat listener
        mBirth.setInputType(InputType.TYPE_NULL);
        mBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECT_IMAGE);
                }
            }
        });


        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Get inserted information
                String name = mFullName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                int sGender = mGender.getCheckedRadioButtonId();
                RadioButton btnGender = findViewById(sGender);
                String gender = btnGender.getText().toString().trim();
                String birth = mBirth.getText().toString().trim();
                String genre = mGenre.getText().toString().trim();
                // fetch email, name and password from intent
                Intent intent = getIntent();
                Bundle bundle = intent.getExtras();
                if(bundle != null ){
                    String emailFromReg = bundle.getString("email", null);
                    String nameFromReg = bundle.getString("name", null);
                    String passFromReg = bundle.getString("password", null);
                    if( emailFromReg==null
                            || nameFromReg==null || passFromReg==null){
                        Toast.makeText(getApplicationContext(),
                                "An error has occurred, re-enter details", Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }
                    if (!name.isEmpty() && !email.isEmpty() && emailFromReg!=null ) {
                        if(emailFromReg.equals(email)){
                            int drawable = (Integer) imgAvatar.getTag();
                            /*if(drawable == R.drawable.avatar_dog){
                                defaultImage();
                            }*/
                            if(pathToImage == null){
                                defaultImage();
                            }
                            uploadVideoToServer(name, email,nameFromReg,passFromReg, phone, gender, birth, genre);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),
                                    "Please enter the correct email !", Toast.LENGTH_LONG)
                                    .show();
                        }

                    }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter your details!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "An unknown error has occurred", Toast.LENGTH_LONG)
                            .show();
                    // The remaining activity in stack is LoginActivity
                    finish();
                }


            }
        });
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
    }

    /**
     * Function to store user in MySQL database will post params to register url
     * */

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year,monthOfYear,dayOfMonth);
        String inp = DateFormat.format("dd/MM/yyyy",c).toString();
        mBirth.setText(inp);
    }
    private void pickerDialog(){
        final Dialog dtPickerDlg = new Dialog(this);
        dtPickerDlg.setContentView(R.layout.picker);
        final DatePicker picker =(DatePicker) dtPickerDlg.findViewById(R.id.datePicker);
        Button btnOk =(Button) dtPickerDlg.findViewById(R.id.okbutton);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtPickerDlg.dismiss();
            }
        });
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

    }
    // upload images methods
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, SetProfileActivity.this);
    }
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(uri != null){
            if(EasyPermissions.hasPermissions(SetProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){

                pathToImage = getRealPathFromURIPath(uri, SetProfileActivity.this);
                dataHandler();
                Log.d(SetProfileActivity.class.getSimpleName(), "Image Path " + pathToImage);
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK  ){

            if(requestCode == SELECT_IMAGE){
                uri = data.getData();
                if (EasyPermissions.hasPermissions(SetProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {


                    pathToImage = getRealPathFromURIPath(uri, SetProfileActivity.this);
                    dataHandler();

                    Log.d(SetProfileActivity.class.getSimpleName(), "Image Path " + pathToImage);
                } else {
                    EasyPermissions.requestPermissions(SetProfileActivity.this, getString(R.string.read_file), READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        }
    }
    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String path = cursor.getString(idx);
            cursor.close();
            return path;
        }
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    private void uploadVideoToServer(final String name, final String email, final String fullName,
                                     final String password,
                                     final String phone, final String gender, final String birth,
                                     final String genre){
        //imgFile = new File(pathToImage);
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imgFile);

        MultipartBody.Part iFile = MultipartBody.Part.createFormData("image", imgFile.getName(), imageBody);
        Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_PATH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        RegisterApi vInterface = retrofit.create(RegisterApi.class);

            // create a map of data to pass along
        RequestBody rFullName = createPartFromString(fullName);
        RequestBody rName = createPartFromString(name);
        RequestBody rPassword = createPartFromString(password);
        RequestBody rEmail = createPartFromString(email);
        RequestBody rPhone = createPartFromString(phone);
        RequestBody rGender = createPartFromString(gender);
        RequestBody rBirth = createPartFromString(birth);
        RequestBody rGenre = createPartFromString(genre);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("full_name", rFullName);
        map.put("name", rName);
        map.put("password", rPassword);
        map.put("email", rEmail);
        map.put("phone", rPhone);
        map.put("gender", rGender);
        map.put("birth", rBirth);
        map.put("genre", rGenre);

        pDialog.setMessage("Registering ...");
        showDialog();

        Call<ProfileModel> serverCom = vInterface.upload(iFile, map);
        serverCom.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(@NonNull Call<ProfileModel> call, @NonNull retrofit2.Response<ProfileModel> response) {
                if(response.isSuccessful()){
                    ProfileModel result = response.body();
                    if(result!=null) {
                        boolean error = result.isError();
                        if(!error) {
                                // User successfully stored in MySQL
                                // Now store the user in sqlite
                            String uid = result.getUid();

                            String full_name = result.getFullName();
                            String mName = result.getName();
                            String email = result.getEmail();
                            String phone = result.getPhone();
                            String gender = result.getGender();
                            String birth = result.getBirth();
                            String genre = result.getGenre();
                            String photo = result.getPhoto();
                            String created_at = result.getCreated();
                                // Inserting row in users table
                            db.addUser(uid, mName, email, full_name, phone, gender, birth, genre, photo, created_at);

                            Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                                // Launch login activity - Only if register successful
                            Intent intent = new Intent(
                                        SetProfileActivity.this,
                                        LoginActivity.class);
                            hideDialog();
                            startActivity(intent);
                            finish();
                        }

                        else {
                        // Error occurred in registration.
                            Log.e(TAG, "Error message " + result.getErrMsg());
                            String errorMsg = result.getErrMsg();
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                            hideDialog();
                        }
                    }
                }

            }
            @Override
            public void onFailure(@NonNull Call<ProfileModel> call, @NonNull Throwable t) {
                Log.e(TAG, "Error message " + t.getMessage());
                Toast.makeText(getApplicationContext(),
                        t.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });
    }
    public static Bitmap loadBitmapFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    private void bitmapToFile(Bitmap bitmap){
        //create a file to write bitmap data
        int quality;
        if(pathToImage!=null){
            imgFile = new File(getApplicationContext().getCacheDir(), pathToImage.substring(pathToImage.lastIndexOf(File.separator)+1));
            quality =100;// other files for high quality
        }
        else{
            quality =0;// needed for compressing png files
            imgFile = new File(getApplicationContext().getCacheDir(), "default_pic.PNG");
        }

        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
//write the bytes in file
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imgFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dataHandler(){
        Bitmap bmp =getResizedBitmap(Objects.requireNonNull(loadBitmapFromFile(new File(pathToImage).getPath())),70,90);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        imgAvatar.setImageBitmap(bmp);
        bitmapToFile(bmp);
    }
    private void defaultImage(){
        Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.avatar_dog);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        bitmapToFile(bmp);
    }
}

