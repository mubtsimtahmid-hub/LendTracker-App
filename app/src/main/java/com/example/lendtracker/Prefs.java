package com.example.lendtracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small helper around SharedPreferences.
 * Saves the "Remember me" setting and the last email on the phone.
 */
public class Prefs {

    private static final String FILE_NAME = "lend_tracker_prefs";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_EMAIL = "saved_email";

    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static void saveLogin(Context context, boolean remember, String email) {
        get(context).edit()
                .putBoolean(KEY_REMEMBER, remember)
                .putString(KEY_EMAIL, remember ? email : "")
                .apply();
    }

    public static boolean isRememberMe(Context context) {
        return get(context).getBoolean(KEY_REMEMBER, false);
    }

    public static String getSavedEmail(Context context) {
        return get(context).getString(KEY_EMAIL, "");
    }

    public static void clearRememberMe(Context context) {
        get(context).edit().putBoolean(KEY_REMEMBER, false).apply();
    }
}
