package com.app.genialnykredyt.Session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class SessionManager {
    private static String isUserLogged = "";
    private static String phoneNumber = "";
    // Shared Preferences
    static SharedPreferences pref;
    static SharedPreferences.Editor editor;
    private static String STORE_VAR_SOCIAL_LOGIN;

    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "MyLoginPref";


    @SuppressLint("WrongConstant")
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public String getUserLogin(){
        return pref.getString("loggedin","");

    }


    public void setUserLogin(String value){
        this.isUserLogged = value;

        editor.putString("loggedin",value);

        editor.commit();

    }

    public static String getPhoneNumber() {
        return pref.getString("phone","");
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        editor.putString("phone",phoneNumber);
        editor.commit();
    }
}
