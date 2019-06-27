package com.earwormfix.earwormfix.Rest;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Preform network calls and compression of video file
 * in background thread in service.
 * Then broadcast to fragment that completed.
 * */
public class AddFeedIntentService extends IntentService {
    public static final String COMPRESS = "compress";
    private static final String TAG = AddFeedIntentService.class.getSimpleName();
    private static final String SERVER_PATH = "https://earwormfix.com";

    private File imgFile;
    private String selectedVideoPath;
    private String pathToStoredVideo;
    private String generatedFilename;
    private String mSaySomething;
    public AddFeedIntentService(){
        super("AddFeedIntentService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AddFeedIntentService(String name) {
        super(name);
        // You don’t want your service to redeliver its process if in any case phone
        // shutdown and application get started
        // If you require such action you can set it to true
        setIntentRedelivery(false);// is default

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String intentAction = Objects.requireNonNull(intent).getAction();
        initFFmpeg();
        selectedVideoPath = intent.getStringExtra("path_to_destination");
        pathToStoredVideo = intent.getStringExtra("path_to_vid");
        mSaySomething = intent.getStringExtra("mSaySomething");
        generatedFilename = intent.getStringExtra("generatedFilename");
        executeCompression();

    }
    private void initFFmpeg(){
        FFmpeg fmpeg =FFmpeg.getInstance(getApplicationContext());
        try {
            fmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
    private void executeCompression(){
        Log.i("PATH","path to video: "+pathToStoredVideo);
        Log.i("PATH","destination path: "+selectedVideoPath);
        //ffmpeg -y -i input.mp4 -s 480x320 -r 20 -c:v libx264 -preset ultrafast -c:a copy -me_method zero -tune fastdecode -tune zerolatency -strict -2 -b:v 1000k -pix_fmt yuv420p output.mp4
        String command = "-y -i "+pathToStoredVideo+" -s 480x320 -r 20 -c:v libx264 -preset ultrafast -c:a copy -me_method zero -tune fastdecode -tune zerolatency -strict -2 -b:v 1000k -pix_fmt yuv420p " +selectedVideoPath;

        String[] args = command.split(" ");
        for (int i= 0;i<args.length;i++){
            Log.i("CMD","args["+i+"]: "+args[i]);
        }
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        try {
            ffmpeg.execute(args, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() { }
                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "on progress: "+message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "on failure: "+message);
                    broadcastResult(false);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Success: "+message);
                    // get a thumbnail frame from video

                    // Start a thread to convert bitmap to file- and then send to server
                    bitmapToFile();
                    if(imgFile !=null){
                        uploadVideoToServer();
                    }
                }
                @Override
                public void onFinish() { }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }
    /**
     * Uploading the video to server
     * */
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }
    private void uploadVideoToServer(){
        File videoFile = new File(selectedVideoPath);

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imgFile);
        RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
        MultipartBody.Part iFile = MultipartBody.Part.createFormData("image", imgFile.getName(), imageBody);
        MultipartBody.Part vFile = MultipartBody.Part.createFormData("video", videoFile.getName(), videoBody);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VideoUploadApi vInterface = retrofit.create(VideoUploadApi.class);
        SQLiteHandler db = new SQLiteHandler(this);
        // get user unique id
        HashMap<String, String> user = db.getUserDetails();
        String userId = user.get("uid");
        String desc = mSaySomething;
        // get video length
        String time = "Unavailable";
        try {
            MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
            retriever.setDataSource(this, Uri.fromFile(videoFile));
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create a map of data to pass along
        RequestBody uid = createPartFromString(userId);
        RequestBody len = createPartFromString(time);
        RequestBody describe = createPartFromString(desc);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("user", uid);
        map.put("length", len);
        map.put("desc", describe);
        // fetch user unique id and video length
        Call<ResultObject> serverCom = vInterface.uploadVideoToServer(vFile, iFile, map);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(@NonNull Call<ResultObject> call, @NonNull Response<ResultObject> response) {
                if(response.isSuccessful()){
                    ResultObject result = response.body();
                    if(result!=null) {
                        Log.d(TAG, "result " + result.getSuccess());
                    }
                }
                Log.d(TAG, "response code " + response.code());
                broadcastResult(true);

            }
            @Override
            public void onFailure(@NonNull Call<ResultObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Error message " + t.getMessage());
                broadcastResult(false);
            }
        });
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
    private void bitmapToFile(){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //Bitmap bmp = getVideoFrame(selectedVideoPath);
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        Bitmap resized = getResizedBitmap(thumb,120,100);
        resized.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Log.d("Async",generatedFilename);
        if(generatedFilename != null){
            imgFile = new File(Environment.getExternalStorageDirectory() + File.separator + generatedFilename+".jpg");
        }
        else {
            Toast.makeText(getApplicationContext(),"An error has happened",Toast.LENGTH_LONG).show();
            broadcastResult(false);
            return;
        }

        try {
            FileOutputStream fo = new FileOutputStream(imgFile);
            fo.write(bytes.toByteArray());
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
            broadcastResult(false);
        }
    }
    private void broadcastResult(boolean stat){
        Intent in = new Intent(COMPRESS);
        // Put extras into the intent as usual
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue",  stat);
        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(in);
    }
}
