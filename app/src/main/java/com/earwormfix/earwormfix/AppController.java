package com.earwormfix.earwormfix;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.earwormfix.earwormfix.Rest.FetchFeedApi;
import com.earwormfix.earwormfix.Rest.FetchFeedApiFactory;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**subclass of the Application class, is instantiated before any other class when the process for the application/package is created
 * Initiate all the volley core objects*/
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private FetchFeedApi restApi;
    private Scheduler scheduler;

    private static AppController mInstance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AppController.context = getApplicationContext();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    private static AppController get(Context context) {
        return (AppController) context.getApplicationContext();
    }
    public static Context getAppContext() {
        return AppController.context;
    }

    public static AppController create(Context context) {
        return AppController.get(context);
    }

    public FetchFeedApi getRestApi() {
        if(restApi == null) {
            restApi = FetchFeedApiFactory.create();
        }
        return restApi;
    }

    public void setRestApi(FetchFeedApi restApi) {
        this.restApi = restApi;
    }

    public Scheduler subscribeScheduler() {
        if (scheduler == null) {
            scheduler = Schedulers.io();
        }

        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
