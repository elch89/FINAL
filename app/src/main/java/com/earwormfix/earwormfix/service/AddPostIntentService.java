package com.earwormfix.earwormfix.service;

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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.earwormfix.earwormfix.Activities.FeedsActivity;
import com.earwormfix.earwormfix.Models.ResultObject;
import com.earwormfix.earwormfix.Rest.VideoUploadApi;
import com.earwormfix.earwormfix.factory.VideoUploadFactory;
import com.earwormfix.earwormfix.helpers.SQLiteHandler;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.earwormfix.earwormfix.AppConfig.CHANNEL_ID;

/**
 * Preform network calls and compression of video file
 * in background threads in service.
 * Then broadcast to fragment that completed.
 * */
public class AddPostIntentService extends IntentService {
    public static final String COMPRESS = "compress";

    private static final String TAG = AddPostIntentService.class.getSimpleName();
    private static final String APP_NAME = "EarwormFix";
    private static final String EXTENSION_JPG = ".jpg";
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
        // Set service to redeliver its process if in any case phone
        // shutdown and application get started
        setIntentRedelivery(false);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        String notiContent = "מעלה את הסרטון\n פעולה זו יכולה לקחת זמן";
        sendNotificationIntent(notiContent);

        db = new SQLiteHandler(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        // good for debugging
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll()
                    /*.penaltyLog()*/
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
        // initialize compression library
        initFfmpeg();
        if(intent != null){
            String action = intent.getAction();
            // get data from AddPost
            pathToStoredVideo = intent.getStringArrayListExtra("params").get(0);//intent.getStringExtra("path_to_vid");
            selectedVideoPath = intent.getStringArrayListExtra("params").get(1);//intent.getStringExtra("path_to_destination");
            mSaySomething = intent.getStringArrayListExtra("params").get(2);//intent.getStringExtra("mSaySomething");
            generatedFilename = intent.getStringArrayListExtra("params").get(3);//intent.getStringExtra("generatedFilename");
            // Start compressing the video file
            if (action != null) {
                if(action.equals(COMPRESS)){

                    Log.e(TAG,"compressing");
                    executeCompression();
                }
            }

        }

    }
    private void initFfmpeg(){
        FFmpeg ffmpeg =FFmpeg.getInstance(getApplicationContext());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
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
        Log.i(TAG,"PATH to video is "+pathToStoredVideo);
        Log.i(TAG,"Destination PATH is "+selectedVideoPath);
        String command = "-n -i "+pathToStoredVideo+
                " -s 480x320 -r 24 -c:v libx264 -preset ultrafast -c:a copy -me_method zero " +
                "-tune fastdecode -tune zerolatency -strict -2 -b:v 1000k -pix_fmt yuv420p " +
                selectedVideoPath;

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
                    // Start a thread to convert bitmap to file
                    // and then send file to server via FTP
                    bitmapToFile();
                    if(imgFile !=null) {

                        // Upload file using ftp(for large files)
                        HashMap<String, String> user = db.getUserDetails();
                        String userId = user.get("uid");
                        AsyncTaskFtp.TaskListener listener = result ->
                        {
                            if(result ){
                                // on success of ftp transmit, send other data to server
                                uploadVideoDetailsToServer();
                            }
                        };
                        AsyncTaskFtp ftpTask = new AsyncTaskFtp(listener);
                        ftpTask.execute(selectedVideoPath,userId);
                        if(ftpTask.isCancelled()){
                            broadcastResult(false);
                        }

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


        // get video length
        String time;
        try {
            File videoFile = new File(selectedVideoPath);
            MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
            retriever.setDataSource(this, Uri.fromFile(videoFile));
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            broadcastResult(false);
            return;
        }
        // Video path on server file system
        String vidFileName = generatedFilename+ ".mp4";
        // create a map of data to pass along
        RequestBody uid = createPartFromString(userId);
        RequestBody len = createPartFromString(time);
        RequestBody describe = createPartFromString(desc);
        RequestBody vidPath = createPartFromString(vidFileName);

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
                        if(result.isStat()){
                            Log.e(TAG, "Error at server " + result.getSuccess());
                            broadcastResult(false);
                        }
                        else {
                            Log.i(TAG, "result " + result.getSuccess());
                            broadcastResult(true);
                        }
                    }
                }
                else{
                    Log.e(TAG, "response failed  ");
                    broadcastResult(true);
                }
                Log.i(TAG, "response code " + response.code());
                // delete compressed video from storage
                if(!deleteTemp(selectedVideoPath)){
                    Log.e(TAG, "Failed to remove file");
                }


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
    // images only, saves a thumbnail in phone
    private void bitmapToFile(){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap thumb,resized;
        thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoPath,
                    MediaStore.Images.Thumbnails.MINI_KIND);

        if(thumb!=null){
            resized = getResizedBitmap(thumb,120,100);
        }
        else { return;}

        resized.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        imgFile = new File(setFileDestinationPath());
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
            sendNotificationIntent("הסתיימה ההעלאה");
        }
        else {
            sendNotificationIntent("ארעה שגיעה בזמן ההעלאה");
        }
        // Put extras into the intent as usual
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue", stat);

        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(in);
    }

    private boolean deleteTemp(String path){
        File file = new File(path);
        return file.delete();
    }
    // sets where to put thumbnail and temp video will be saved on device
    private String setFileDestinationPath(){
        String filePathEnvironment = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d(TAG, "Full path edited " + filePathEnvironment + "/earwormfix/" + generatedFilename + EXTENSION_JPG);
        return filePathEnvironment+ "/earwormfix/" + generatedFilename + EXTENSION_JPG;
    }


    private static class AsyncTaskFtp extends AsyncTask<String, String, Void> {
        // string[0] ---> selectedVideoPath
        // strings[1] ---> userId
        // This is the reference to the associated listener
        public interface TaskListener {
            void onFinished(boolean result);
        }

        private final TaskListener taskListener;
        AsyncTaskFtp(TaskListener listener) {
            // The listener reference is passed in through the constructor
            this.taskListener = listener;
        }
        @Override
        protected Void doInBackground(String... strings) {
            // Send file via ftp on asyncTask
            FTPClient client = new FTPClient();
            try {
                client.connect("ftp.earwormfix.com", 21);
                client.login("XXXX", "XXXXX"); // TODO: change when testing

                client.sendNoOp(); //used so server timeout exception will not rise
                int reply = client.getReplyCode();
                if(!FTPReply.isPositiveCompletion(reply)){
                    client.disconnect();
                    Log.e(TAG,"FTP server refuse connection Code- "+ reply);
                    this.taskListener.onFinished(false);
                    cancel(true);
                }
                client.enterLocalPassiveMode();//Switch to passive mode
                client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                String remoteFile = "post/"+strings[1] +
                        File.separator +
                        strings[0].substring(strings[0].lastIndexOf(File.separator)+1);
                File videoFile = new File(strings[0]);
                InputStream inputStream = new FileInputStream(videoFile);

                Log.i(TAG,"storing...");
                boolean stored = client.storeFile(remoteFile, inputStream);
                if (stored) {
                    Log.i(TAG,"Done!!!");
                }
                else{
                    Log.e(TAG,"Failed to send ftp "+ reply);
                    this.taskListener.onFinished(false);
                    inputStream.close();
                    client.logout();
                    cancel(true);
                }
                inputStream.close();
                //logout will close the connection
                client.logout();
            } catch (IOException e) {
                e.printStackTrace();
                this.taskListener.onFinished(false);
                cancel(true);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(this.taskListener!=null){
                this.taskListener.onFinished(true);
            }
        }
    }

    private void sendNotificationIntent( String notiContent){
        Intent intent = new Intent(this, FeedsActivity.class);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pIntent = PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // build notification
        Notification n;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            n = new NotificationCompat.Builder(AddPostIntentService.this,CHANNEL_ID)
                    .setContentTitle(APP_NAME)
                    .setContentText(notiContent)
                    .setSmallIcon(android.R.drawable.ic_menu_upload_you_tube)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID)
                    /*.setWhen(System.currentTimeMillis())*/
                    .build();
        }
        else{
            n = new Notification.Builder(this)
                    .setTicker("File uploading")
                    .setContentTitle(APP_NAME)
                    .setContentText(notiContent)
                    .setSmallIcon(android.R.drawable.ic_menu_upload_you_tube)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    /*.setWhen(System.currentTimeMillis())*/
                    .build();
        }
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"Service destroyed");
    }
}
