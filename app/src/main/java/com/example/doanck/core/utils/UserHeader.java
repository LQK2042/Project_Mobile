package com.example.doanck.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.doanck.R;
import com.example.doanck.core.network.ApiClient;
import com.example.doanck.core.network.ProfileApi;
import com.example.doanck.data.model.ProfileResponse;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public final class UserHeader {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_USER_AVATAR_URL = "user_avatar_url";
    private static final String KEY_USER_CREATED_AT = "user_created_at";
    private static final String TAG = "UserHeader";

    private UserHeader() {
    }

    public static void saveUserInfo(Context context, String email, String name, long createdAtMillis, int avatarResId, String avatarUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .putInt(KEY_USER_AVATAR, avatarResId)
                .putString(KEY_USER_AVATAR_URL, avatarUrl)
                .putLong(KEY_USER_CREATED_AT, createdAtMillis)
                .apply();
    }

    public static void saveUserInfo(Context context, String email, String name, long createdAtMillis, int avatarResId) {
        saveUserInfo(context, email, name, createdAtMillis, avatarResId, null);
    }

    public static void saveUserInfo(Context context, String email, String name, int avatarResId) {
        saveUserInfo(context, email, name, System.currentTimeMillis(), avatarResId, null);
    }

    public static void saveUserInfo(Context context, String email, String name) {
        saveUserInfo(context, email, name, System.currentTimeMillis(), R.drawable.acount_avt, null);
    }

    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString(KEY_USER_EMAIL, "");
        return email == null ? "" : email;
    }

    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        return name == null ? "" : name;
    }

    public static int getUserAvatar(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_AVATAR, R.drawable.acount_avt);
    }

    public static long getUserCreatedAt(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_USER_CREATED_AT, System.currentTimeMillis());
    }

    public static String getUserAvatarUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String url = prefs.getString(KEY_USER_AVATAR_URL, "");
        return url == null ? "" : url;
    }

    public interface UserHeaderListener {
        void onUnauthorized();
    }

    public static void displayUserInfo(
            Context context,
            ImageView imageViewAvatar,
            TextView textViewName,
            TextView textViewEmail,
            TextView textViewGreeting
    ) {
        displayUserInfo(context, imageViewAvatar, textViewName, textViewEmail, textViewGreeting, null);
    }

    public static void displayUserInfo(
            Context context,
            ImageView imageViewAvatar,
            TextView textViewName,
            TextView textViewEmail,
            TextView textViewGreeting,
            UserHeaderListener listener
    ) {
        applyCachedUserInfo(context, imageViewAvatar, textViewName, textViewEmail, textViewGreeting);
        requestProfileUpdate(context, imageViewAvatar, textViewName, textViewEmail, textViewGreeting, listener);
    }

    private static void applyCachedUserInfo(
            Context context,
            ImageView imageViewAvatar,
            TextView textViewName,
            TextView textViewEmail,
            TextView textViewGreeting
    ) {
        if (imageViewAvatar != null) {
            String avatarUrl = getUserAvatarUrl(context);
            Log.d(TAG, "Avatar URL from cache: " + avatarUrl);
            if (avatarUrl != null && !avatarUrl.isEmpty() && (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"))) {
                Log.d(TAG, "Loading avatar from URL: " + avatarUrl);
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.acount_avt)
                        .error(R.drawable.acount_avt)
                        .centerCrop()
                        .into(imageViewAvatar);
            } else {
                Log.d(TAG, "Using default avatar resource");
                imageViewAvatar.setImageResource(R.drawable.acount_avt);
            }
        }
        if (textViewName != null) {
            String name = getUserName(context);
            textViewName.setText(name.isEmpty() ? "User" : name);
        }
        if (textViewEmail != null) {
            String email = getUserEmail(context);
            textViewEmail.setText(email.isEmpty() ? "user@example.com" : email);
        }
        if (textViewGreeting != null) {
            textViewGreeting.setText(getGreeting());
        }
    }

    private static void requestProfileUpdate(
            Context context,
            ImageView imageViewAvatar,
            TextView textViewName,
            TextView textViewEmail,
            TextView textViewGreeting,
            UserHeaderListener listener
    ) {
        ProfileApi profileApi = ApiClient.getProfileApi();
        profileApi.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.code() == 401) {
                    Log.w(TAG, "Profile fetch failed: HTTP 401 - clearing cached user");
                    clearUserInfo(context);
                    applyCachedUserInfo(context, imageViewAvatar, textViewName, textViewEmail, textViewGreeting);
                    if (listener != null) {
                        listener.onUnauthorized();
                    }
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Profile fetch failed: HTTP " + response.code());
                    return;
                }

                ProfileResponse body = response.body();
                if (!body.success || body.data == null) {
                    Log.w(TAG, "Profile fetch returned success=false or data=null");
                    return;
                }

                String email = body.data.account == null ? "" : body.data.account;
                String fullName = buildFullName(body.data.firstName, body.data.lastName);
                String imageUrl = body.data.imageURL;
                Log.d(TAG, "Profile API response - email: " + email + ", name: " + fullName + ", imageURL: " + imageUrl);
                saveUserInfo(context, email, fullName.isEmpty() ? "User" : fullName, System.currentTimeMillis(), R.drawable.acount_avt, imageUrl);
                applyCachedUserInfo(context, imageViewAvatar, textViewName, textViewEmail, textViewGreeting);
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "Profile fetch error", t);
            }
        });
    }

    private static String buildFullName(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            builder.append(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(lastName.trim());
        }
        return builder.toString();
    }

    public static boolean isUserLoggedIn(Context context) {
        return !getUserEmail(context).isEmpty();
    }

    /**
     * Force refresh user profile from API (useful for debugging)
     */
    public static void refreshUserProfile(Context context, UserHeaderListener listener) {
        ProfileApi profileApi = ApiClient.getProfileApi();
        Log.d(TAG, "Forcing profile refresh from API...");
        profileApi.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.code() == 401) {
                    Log.w(TAG, "refreshUserProfile: HTTP 401 - Not authenticated");
                    if (listener != null) {
                        listener.onUnauthorized();
                    }
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "refreshUserProfile: HTTP " + response.code());
                    return;
                }

                ProfileResponse body = response.body();
                if (!body.success || body.data == null) {
                    Log.w(TAG, "refreshUserProfile: success=false or data=null");
                    return;
                }

                String email = body.data.account == null ? "" : body.data.account;
                String fullName = buildFullName(body.data.firstName, body.data.lastName);
                String imageUrl = body.data.imageURL;
                Log.d(TAG, "refreshUserProfile success - email: " + email + ", name: " + fullName + ", imageURL: " + imageUrl);
                saveUserInfo(context, email, fullName.isEmpty() ? "User" : fullName, System.currentTimeMillis(), R.drawable.acount_avt, imageUrl);
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "refreshUserProfile error", t);
            }
        });
    }

    public static void clearUserInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private static String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour <= 11) {
            return "Good Morning";
        } else if (hour >= 12 && hour <= 17) {
            return "Good Afternoon";
        } else if (hour >= 18 && hour <= 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }
}
