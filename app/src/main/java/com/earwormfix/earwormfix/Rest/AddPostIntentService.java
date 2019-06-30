package com.earwormfix.earwormfix.Rest;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.earwormfix.earwormfix.Activities.FeedsActivity;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * Preform network calls and compression of video file
 * in background thread in service.
 * Then broadcast to fragment that completed.
 * */
public class AddPostIntentService extends IntentService {
    public static final String COMPRESS = "compress";
    private static final String TAG = AddPostIntentService.class.getSimpleName();
    private static final String APP_NAME = "EarwormFix";
    private SQLiteHandler db;
    private File imgFile;
    private String selectedVideoPath;
    private String pathToStoredVideo;
    private String generatedFilename;
    private String mSaySomething;


    public AddPostIntentService(){
        super("AddPostIntentService");
    }
    public AddPostIntentService(String name) {
        super(name);
        // You don’t want your service to redeliver its process if in any case phone
        // shutdown and application get started
        // If you require such action you can set it to true
        setIntentRedelivery(false);// is default

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);


        }
        String notiContent = "מעלה לך את הסרטון, פעולה זו יכולה לקחת זמן";
        sendNotificationIntent(APP_NAME,notiContent);
        db = new SQLiteHandler(this);
        String intentAction = Objects.requireNonNull(intent).getAction();
        initFFmpeg();
        selectedVideoPath = intent.getStringExtra("path_to_destination");
        pathToStoredVideo = intent.getStringExtra("path_to_vid");
        mSaySomething = intent.getStringExtra("mSaySomething");
        generatedFilename = intent.getStringExtra("generatedFilename");
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)*/
            executeCompression();
       /* else{// skip compressing
            bitmapToFile();
            HashMap<String, String> user = db.getUserDetails();
            String userId = user.get("uid");
            AsyncTaskFtp.TaskListener listener = result ->
            {
                if(result.equals("Success")){
                    uploadVideoDetailsToServer();
                }
                else{
                    broadcastResult(false);
                }
            };
            AsyncTaskFtp ftpTask = new AsyncTaskFtp(listener);
            ftpTask.execute(pathToStoredVideo,userId);
        }*/




    }
    private void initFFmpeg(){
        FFmpeg fmpeg =FFmpeg.getInstance(getApplicationContext());
        try {
            fmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onStart() {}

                @Override
                public void onFailure() {broadcastResult(false);}

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
        String command = "-n -i "+pathToStoredVideo+" -s 480x320 -r 24 -c:v libx264 -preset ultrafast -c:a copy -me_method zero -tune fastdecode -tune zerolatency -strict -2 -b:v 1000k -pix_fmt yuv420p " +selectedVideoPath;

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
                    // Start a thread to convert bitmap to file-
                    // and then send data to server
                    bitmapToFile();
                    if(imgFile !=null) {

                        // Upload file using ftp(for large files)
                        HashMap<String, String> user = db.getUserDetails();
                        String userId = user.get("uid");
                        AsyncTaskFtp.TaskListener listener = result ->
                        {
                            if(result.equals("Success")){
                                uploadVideoDetailsToServer();
                            }
                            else{
                                broadcastResult(false);
                            }
                        };
                        AsyncTaskFtp ftpTask = new AsyncTaskFtp(listener);
                        ftpTask.execute(selectedVideoPath,userId);

                    }
                    else {
                        broadcastResult(false);
                    }
                }
                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running

        }
    }
    private String tempFile;
    /**
     * Uploading the video to server
     * */
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }
    private void uploadVideoDetailsToServer(){
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imgFile);
        MultipartBody.Part iFile = MultipartBody.Part.createFormData("image", imgFile.getName(), imageBody);



        // get user unique id
        HashMap<String, String> user = db.getUserDetails();
        String userId = user.get("uid");
        String desc = mSaySomething;
        VideoUploadApi vInterface = VideoUploadFactory.create();
        // Video path on server file system
        String vidPathBackEnd = generatedFilename+ ".mp4";
        // get video length
        String time = "Unavailable";
        try {
            File videoFile = new File(selectedVideoPath);
            MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
            retriever.setDataSource(this, Uri.fromFile(videoFile));
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            broadcastResult(false);
        }

        // create a map of data to pass along
        RequestBody uid = createPartFromString(userId);
        RequestBody len = createPartFromString(time);
        RequestBody describe = createPartFromString(desc);
        RequestBody vidPath = createPartFromString(vidPathBackEnd);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("path",vidPath);
        map.put("user", uid);
        map.put("length", len);
        map.put("desc", describe);
        // fetch user unique id and video length
        Call<ResultObject> serverCom = vInterface.uploadVideoToServer( iFile, map);
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
                // make as cache in future
                if(!deleteTemp(selectedVideoPath)){
                    Log.e(TAG, "Failed to remove file");
                }

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
    // images only
    private void bitmapToFile(){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap thumb,resized;
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){*/
            thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoPath,
                    MediaStore.Images.Thumbnails.MINI_KIND);
       /* }*/
        /*else{
            thumb = ThumbnailUtils.createVideoThumbnail( pathToStoredVideo,
                    MediaStore.Images.Thumbnails.MINI_KIND);
        }*/
        if(thumb!=null){
            resized = getResizedBitmap(thumb,120,100);
        }
        else return;

        resized.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        if(generatedFilename != null){
            imgFile = new File(setFileDestinationPath(".jpg"));
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
        if(stat){
            sendNotificationIntent(APP_NAME,"הסתיימה ההעלאה");
        }
        else {
            sendNotificationIntent(APP_NAME,"ארעה שגיעה בזמן ההעלאה");
        }
        // Put extras into the intent as usual
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue",  stat);
        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(in);
    }
    private boolean deleteTemp(String path){
        File file = new File(path);
        return file.delete();
    }
    private String setFileDestinationPath(String extension){
        String filePathEnvironment = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d(TAG, "Full path edited " + filePathEnvironment + "/earwormfix/" + generatedFilename + extension);
        return filePathEnvironment+ "/earwormfix/" + generatedFilename + extension;
    }
    private static class AsyncTaskFtp extends AsyncTask<String, String, String> {
        // string[0] ---> selectedVideoPath
        // strings[1] ---> userId
        // This is the reference to the associated listener
        public interface TaskListener {
            public void onFinished(String result);
        }

        private final TaskListener taskListener;
        public AsyncTaskFtp(TaskListener listener) {
            // The listener reference is passed in through the constructor
            this.taskListener = listener;
        }
        @Override
        protected String doInBackground(String... strings) {
            FTPClient client = new FTPClient();
            try {
                client.connect("ftp.earwormfix.com", 21);
                client.login("u561050024.vm229px", "NFB6kBZXWrg5");

                client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                String remoteFile = "post/"+strings[1] +
                        File.separator +
                        strings[0].substring(strings[0].lastIndexOf(File.separator)+1);
                File videoFile = new File(strings[0]);
                InputStream inputStream = new FileInputStream(videoFile);

                Log.d(TAG,"storing....");
                boolean done = client.storeFile(remoteFile, inputStream);
                inputStream.close();
                if (done) {
                    Log.d(TAG,"Done!!!!!!!!!!!!!!!");
                }
                else {
                    this.taskListener.onFinished("Fail");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(this.taskListener!=null){
                this.taskListener.onFinished("Success");
            }
        }
    }

    private void sendNotificationIntent(String notiTitle, String notiContent){
        Intent intent = new Intent(this, FeedsActivity.class);
        intent.putExtra("testing","testing");
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pIntent = PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            n = new Notification.Builder(AddPostIntentService.this)
                    .setTicker("testing")
                    .setContentTitle(notiTitle)
                    .setContentText(notiContent)
                    .setSmallIcon(android.R.drawable.ic_menu_view)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setChannelId("some_chanel_id")
                    /*.setWhen(System.currentTimeMillis())*/
                    .build();
        }
        else{
            n = new Notification.Builder(this)
                    .setTicker("testing")
                    .setContentTitle(notiTitle)
                    .setContentText(notiContent)
                    .setSmallIcon(android.R.drawable.ic_menu_view)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    /*.setWhen(System.currentTimeMillis())*/
                    .build();
        }
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationManager noti = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0, n);
    }
}
