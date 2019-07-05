package com.earwormfix.earwormfix;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**subclass of the Application class, is instantiated before any other class when the process for the application/package is created
 * Initiate all the volley core objects*/
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }
    // Set up a Request queue for volley, which manages worker threads for running the network operations
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }
    // Add Request objects to the queue
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
    // Cancel requests
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    // get Instance of appController
    private static AppController get(Context context) {
        return (AppController) context.getApplicationContext();
    }
    // get application Context for using in classes where context cannot be fetched
    public static AppController getAppContext() {
        return mInstance;
    }
    // create an appController based on given app context
    public static AppController create(Context context) {
        return AppController.get(context);
    }



}
