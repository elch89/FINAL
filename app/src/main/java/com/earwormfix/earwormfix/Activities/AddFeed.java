package com.earwormfix.earwormfix.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.ResultObject;
import com.earwormfix.earwormfix.Rest.VideoUploadApi;
import com.earwormfix.earwormfix.helper.SQLiteHandler;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** An activity to add feeds */
public class AddFeed extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private static final String TAG = AddFeed.class.getSimpleName();
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    private static final int READ_REQUEST_CODE = 200;
    private static final int SELECT_VIDEO = 1;
    private Uri uri;
    private String pathToStoredVideo;
    private VideoView displayRecordedVideo;
    private String selectedVideoPath;
    private static final String SERVER_PATH = "https://earwormfix.com";

    SQLiteHandler db;

    private EditText mSaySomething;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds_add);
        mSaySomething = findViewById(R.id.say_something);
        displayRecordedVideo = (VideoView)findViewById(R.id.video_display);
        final Button captureVideoButton = (Button)findViewById(R.id.capture_video);
        final Button loadFromDevice = findViewById(R.id.upload_video);
        db = new SQLiteHandler(this);
        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask for permission to take video from device
                Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(videoCaptureIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        });
        loadFromDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECT_VIDEO);
                }
            }
        });



        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddFeed.this);

                // Add the buttons
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent replyIntent = new Intent();
                        // User clicked OK button
                        if(pathToStoredVideo == null) {
                            Log.e("error","selected video path = null!");

                        } else {
                            /** send to server
                             * selectedVideoPath is path to the selected video
                             */
                            //Store the video to server

                            uploadVideoToServer(pathToStoredVideo);

                            // send intent with video id?
                            String word = mSaySomething.getText().toString();
                            replyIntent.putExtra("uid", word);
                            setResult(RESULT_OK, replyIntent);
                            //}
                            finish();
                        }



                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // Set message to display
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);
                // Create dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                //if (TextUtils.isEmpty(mEditWordView.getText()))
                 //   setResult(RESULT_CANCELED, replyIntent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK  ){
            if (requestCode == SELECT_VIDEO) {
                pathToStoredVideo = getDevicePath(data.getData());

                /*try {*/
                    /*if(selectedVideoPath == null) {
                        Log.e("error","selected video path = null!");
                        finish();
                    } else {
                        *//**
                         * try to do something there - send to server
                         * selectedVideoPath is path to the selected video
                         *//*
                        uploadVideoToServer(selectedVideoPath);
                    }*/
                /*} catch (IOException e) {
                    //#debug
                    e.printStackTrace();
                }*/
            }
            else if(requestCode == REQUEST_VIDEO_CAPTURE){
                uri = data.getData();
                if (EasyPermissions.hasPermissions(AddFeed.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    displayRecordedVideo.setVideoURI(uri);
                    displayRecordedVideo.start();

                    pathToStoredVideo = getRealPathFromURIPath(uri, AddFeed.this);
                    Log.d(TAG, "Recorded Video Path " + pathToStoredVideo);
                    //Store the video to server
                    //uploadVideoToServer(pathToStoredVideo);

                } else {
                    EasyPermissions.requestPermissions(AddFeed.this, getString(R.string.read_file), READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        }
    }
    private String getFileDestinationPath(){
        String generatedFilename = String.valueOf(System.currentTimeMillis());
        String filePathEnvironment = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File directoryFolder = new File(filePathEnvironment + "/video/");
        if(!directoryFolder.exists()){
            directoryFolder.mkdir();
        }
        Log.d(TAG, "Full path " + filePathEnvironment + "/video/" + generatedFilename + ".mp4");
        return filePathEnvironment + "/video/" + generatedFilename + ".mp4";
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, AddFeed.this);
    }
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(uri != null){
            if(EasyPermissions.hasPermissions(AddFeed.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                displayRecordedVideo.setVideoURI(uri);
                displayRecordedVideo.start();

                pathToStoredVideo = getRealPathFromURIPath(uri, AddFeed.this);
                Log.d(TAG, "Recorded Video Path " + pathToStoredVideo);
                //Store the video to your server
                //uploadVideoToServer(pathToStoredVideo);

            }
        }
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "User has denied requested permission");
    }

    /**
     * Uploading the video to server
     * */
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }
    private void uploadVideoToServer(String pathToVideoFile){
        File videoFile = new File(pathToVideoFile);
        RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
        MultipartBody.Part vFile = MultipartBody.Part.createFormData("video", videoFile.getName(), videoBody);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VideoUploadApi vInterface = retrofit.create(VideoUploadApi.class);

        // get user unique id
        HashMap<String, String> user = db.getUserDetails();
        String userId = user.get("uid");
        // get video length
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.fromFile(videoFile));
        // time in Micro seconds
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        // create a map of data to pass along
        RequestBody uid = createPartFromString(userId);
        RequestBody len = createPartFromString(time);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("user", uid);
        map.put("length", len);
        // fetch user unique id and video length
        Call<ResultObject> serverCom = vInterface.uploadVideoToServer(vFile, map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(@NonNull Call<ResultObject> call, @NonNull Response<ResultObject> response) {
                ResultObject result = response.body();
                Log.d("response","-------------------------------"+ response.code() );
                /*Log.d("result is: ", String.valueOf(response));
                assert result != null;
                if(!TextUtils.isEmpty(result.getSuccess())){
                    Toast.makeText(AddFeed.this, "Result " + result.getSuccess(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Result " + result.getSuccess());
                }*/
            }
            @Override
            public void onFailure(@NonNull Call<ResultObject> call, @NonNull Throwable t) {
                Log.d(TAG, "Error message " + t.getMessage());
            }
        });
    }

    /**
     * Get the path of video on device
     * @param contentURI
     * @param activity
     * @return path
     */
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
    public String getDevicePath(Uri uri) {//unite with previous method?
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
}
