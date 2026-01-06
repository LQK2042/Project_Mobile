package com.example.doanck.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple holder for session cookies so Retrofit can attach them to every request.
 */
public final class SessionManager {

    private static final String PREFS_NAME = "user_session";
    private static final String KEY_COOKIE = "cookie";
    private static Context appContext;

    private SessionManager() {
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void saveCookie(Context context, String cookie) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_COOKIE, cookie)
                .apply();
    }

    public static void saveCookie(String cookie) {
        if (appContext == null) {
            return;
        }
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_COOKIE, cookie)
                .apply();
    }

    public static String getCookie(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COOKIE, "");
    }

    public static String getCookie() {
        if (appContext == null) {
            return "";
        }
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COOKIE, "");
    }
}
