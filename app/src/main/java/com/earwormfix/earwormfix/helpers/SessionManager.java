package com.earwormfix.earwormfix.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
/**Maintains session data across the app using the SharedPreferences.
 * We store a boolean flag isLoggedIn in shared preferences to check the login status*/

public class SessionManager {

    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    // Shared pref mode


    // Shared preferences file name
    private static final String PREF_NAME = "EarwormFixLogin";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final int PRIVATE_MODE = 0;
    private static final String TAG = SessionManager.class.getSimpleName();

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
