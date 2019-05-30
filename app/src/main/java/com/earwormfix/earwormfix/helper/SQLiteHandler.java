package com.earwormfix.earwormfix.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**This class takes care of storing the user data in SQLite database.
 * Whenever we needs to get the logged in user information, we fetch
 * from SQLite instead of making request to server*/
public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_PROFILE = "profile";


    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";

    // Profile Table Columns names
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTH = "birth";
    private static final String KEY_GENRE = "genre";
    private static final String KEY_AVATAR = "avatar";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }//change version to 1 after clearing cache----DATABASE_VERSION

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_PROFILE_TABLE = "CREATE TABLE " + TABLE_PROFILE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_FULL_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"+ KEY_PHONE + " TEXT,"+ KEY_GENDER + " TEXT,"
                + KEY_BIRTH + " TEXT,"+ KEY_GENRE + " TEXT,"+ KEY_AVATAR + " TEXT,"
                + KEY_UID + " TEXT," + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_PROFILE_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id+" ----> ");
    }
    /**
     * Storing user details in database
     * */
    public void addProfile(String fullName, String email, String phone, String gender, String birth,
                           String genre, String avatar, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FULL_NAME, fullName); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_PHONE, phone);
        values.put(KEY_GENDER, gender);
        values.put(KEY_BIRTH, birth);
        values.put(KEY_GENRE, genre);
        values.put(KEY_AVATAR, avatar);
        values.put(KEY_UID, uid); //
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_PROFILE, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New profile inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }
    /**
     * Getting user profile data from database
     * */
    public HashMap<String, String> getProfileDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_PROFILE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("full_name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("phone", cursor.getString(3));
            user.put("gender", cursor.getString(4));
            user.put("birth", cursor.getString(5));
            user.put("genre", cursor.getString(6));
            user.put("avatar", cursor.getString(7));
            user.put("uid", cursor.getString(8));
            user.put("created_at", cursor.getString(9));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user profile from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
    public void deleteProfiles() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_PROFILE, null, null);
        db.close();

        Log.d(TAG, "Deleted all profile info from sqlite");
    }

    public void updateProfile(String col, String email, String param, String updated_at){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col,param);
        String[] arg = new String[]{email};
        // Updating Row
        db.update(TABLE_PROFILE,  values, "email=?",arg);
        db.close(); // Closing database connection
        Log.d(TAG, "Updated all profile info from sqlite");


    }
}
