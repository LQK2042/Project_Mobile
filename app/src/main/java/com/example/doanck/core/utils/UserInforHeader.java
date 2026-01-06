package com.example.doanck.core.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Thin alias kept for assignments that still reference UserInforHeader.
 */
public final class UserInforHeader {
    private UserInforHeader() {}

    public static void saveUserInfo(Context context, String email, String name) {
        UserHeader.saveUserInfo(context, email, name);
    }

    public static void saveUserInfo(Context context, String email, String name, int avatarResId) {
        UserHeader.saveUserInfo(context, email, name, avatarResId);
    }

    public static void saveUserInfo(Context context, String email, String name, long createdAtMillis, int avatarResId) {
        UserHeader.saveUserInfo(context, email, name, createdAtMillis, avatarResId);
    }

    public static void displayUserInfo(Context context, ImageView avatar, TextView name, TextView email, TextView greeting) {
        UserHeader.displayUserInfo(context, avatar, name, email, greeting);
    }

    public static boolean isUserLoggedIn(Context context) {
        return UserHeader.isUserLoggedIn(context);
    }

    public static void clearUserInfo(Context context) {
        UserHeader.clearUserInfo(context);
    }

    public static String getUserEmail(Context context) {
        return UserHeader.getUserEmail(context);
    }

    public static String getUserName(Context context) {
        return UserHeader.getUserName(context);
    }

    public static int getUserAvatar(Context context) {
        return UserHeader.getUserAvatar(context);
    }

    public static long getUserCreatedAt(Context context) {
        return UserHeader.getUserCreatedAt(context);
    }
}

