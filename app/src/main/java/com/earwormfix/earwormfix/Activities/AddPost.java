package com.earwormfix.earwormfix.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Rest.AddPostIntentService;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** An activity to add feeds */
public class AddPost extends AppCompatActivity {
    private static final String TAG = AddPost.class.getSimpleName();
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final int REQUEST_SELECT_VIDEO = 1;
    private Uri uri;
    private String pathToStoredVideo;
    private SimpleExoPlayer displayRecordedVideo;
    private PlayerView playerView;
    private String selectedVideoPath;
    private AspectRatioFrameLayout aspectRatioFrameLayout;
    private String generatedFilename;
    private EditText mSaySomething;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds_add);
        aspectRatioFrameLayout = (AspectRatioFrameLayout)findViewById(R.id.videoView1);
        mSaySomething = (EditText)findViewById(R.id.say_something);
        playerView = (PlayerView)findViewById(R.id.video_display);
        Button captureVideoButton = (Button)findViewById(R.id.capture_video);
        Button loadFromDevice = (Button)findViewById(R.id.upload_video);
        Button btnSave = (Button)findViewById(R.id.button_save);
        ImageView volumeOff =(ImageView)findViewById(R.id.exo_volume_off);
        ImageView volumeOn = (ImageView)findViewById(R.id.exo_volume_up);

        // Volume control mute/un mute
        volumeOn.setVisibility(View.INVISIBLE);
        volumeOff.setOnClickListener(v -> {
            if(displayRecordedVideo!=null){
                displayRecordedVideo.setVolume(0f);
                volumeOn.setVisibility(View.VISIBLE);
                volumeOff.setVisibility(View.INVISIBLE);
            }
        });

        volumeOn.setOnClickListener(v -> {
            if(displayRecordedVideo!=null) {
                displayRecordedVideo.setVolume(0.75f);
                volumeOn.setVisibility(View.INVISIBLE);
                volumeOff.setVisibility(View.VISIBLE);
            }
        });

        /**
         * Button listeners
         * Permissions were requested on first login
         * */
        // take video from device
        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(videoCaptureIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        });
        // take video from external memory
        loadFromDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, REQUEST_SELECT_VIDEO);
                }
            }
        });
        // Save selected and post
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Construct a new Alert Dialog box
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPost.this);
                // Add the buttons, positive/negetive
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        if(pathToStoredVideo == null) {
                            Log.e(TAG,"selected video path = null!");
                            Toast.makeText(getApplicationContext(),"NO VIDEO WAS SELECTED",Toast.LENGTH_LONG).show();
                        } else {
                            /**
                             * send to server via IntentService - background heavy operation
                             * pathToStoredVideo - is path to video we want to upload i.e input
                             * selectedVideoPath - is path to the compressed video i.e output
                             */
                            // test if the queue works!!!!!!!!!!!
                            String[] array = {pathToStoredVideo, selectedVideoPath, mSaySomething.getText().toString(), generatedFilename};
                            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(array));

                            selectedVideoPath = getFileDestinationPath();
                            Intent compressIntent = new Intent(AddPost.this, AddPostIntentService.class);
                            compressIntent.setAction(AddPostIntentService.COMPRESS);
                            /*compressIntent.putExtra("path_to_vid", pathToStoredVideo);
                            compressIntent.putExtra("path_to_destination", selectedVideoPath);
                            compressIntent.putExtra("mSaySomething", mSaySomething.getText().toString());
                            compressIntent.putExtra("generatedFilename", generatedFilename);*/
                            compressIntent.putStringArrayListExtra("params",arrayList);
                            startService(compressIntent);
                            Intent upIntent = new Intent(AddPost.this, AddPostIntentService.class);
                            upIntent.setAction(AddPostIntentService.UPLOAD_FILE);
                            upIntent.putStringArrayListExtra("params",arrayList);
                            startService(upIntent);
                            Intent intent = new Intent(AddPost.this, AddPostIntentService.class);
                            intent.setAction(AddPostIntentService.COMPRESS);
                            intent.putStringArrayListExtra("params",arrayList);
                            startService(intent);
                            finish();

                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // User cancelled the dialog
                    dialog.dismiss();
                });
                // Set message to display
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);
                // Create dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK  ){
            if(requestCode == REQUEST_VIDEO_CAPTURE || requestCode == REQUEST_SELECT_VIDEO){
                uri = data.getData();
                initializePlayer();
                pathToStoredVideo = getRealPathFromURIPath(uri, AddPost.this);
                Log.d(TAG, "Recorded Video Path " + pathToStoredVideo);
            }
        }
    }
    /** Sets path to download compressed file in main external storage - /storage/emulated/0/
     * and creates a new folder if does not exist named earwormfix - for caching reasons
     * */
    private String getFileDestinationPath(){
        generatedFilename = String.valueOf(System.currentTimeMillis());
        String filePathEnvironment = Environment.getExternalStorageDirectory().getAbsolutePath();
        File directoryFolder = new File(filePathEnvironment + "/earwormfix/");
        if(!directoryFolder.exists()){
            directoryFolder.mkdir();
        }
        Log.d(TAG, "Destination path " + filePathEnvironment + "/earwormfix/" + generatedFilename + ".mp4");
        return filePathEnvironment+ "/earwormfix/" + generatedFilename + ".mp4";
    }
    /**
     * Get the path of video on device
     */
    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
            String path = cursor.getString(idx);
            cursor.close();
            return path;
        }
    }
    // Init media player
    private void initializePlayer() {
        displayRecordedVideo = ExoPlayerFactory.newSimpleInstance(this,
                        new DefaultTrackSelector());

        playerView.setPlayer(displayRecordedVideo);

        displayRecordedVideo.setPlayWhenReady(false);
        displayRecordedVideo.seekTo(0, 0);
        //start();
    }
    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(this,"video")).
                createMediaSource(uri);
    }
    public void start(){
        MediaSource mediaSource = buildMediaSource(uri);
        displayRecordedVideo.prepare(mediaSource, true, false);
        displayRecordedVideo.setVolume(0f);
        displayRecordedVideo.setPlayWhenReady(true);
    }

    public void stop(){
        displayRecordedVideo.stop(true);
    }
    public void release(){
        displayRecordedVideo.release();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(displayRecordedVideo != null){
            stop();
            release();
        }

    }
    ///????????????????????????????????????????????????????////
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(uri!=null)
            stop();
            //initializePlayer();
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerView.getLayoutParams();
            params.width= ViewGroup.LayoutParams.MATCH_PARENT;
            params.height= ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.setLayoutParams(params);
            aspectRatioFrameLayout.setAspectRatio(18f/9f);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
           // do something
        }
    }

}
