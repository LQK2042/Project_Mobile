package com.example.doanck.feature.profile;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doanck.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileAvatar;
    private TextView profileUsername;
    private TextView profileEmail;
    private TextView profileCreatedDate;

    private static final String API_URL = "https://midtermhehe.azurewebsites.net/api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserProfile();
    }

    private void initViews() {
        profileAvatar = findViewById(R.id.profile_avatar);
        profileUsername = findViewById(R.id.profile_username);
        profileEmail = findViewById(R.id.profile_email);
        profileCreatedDate = findViewById(R.id.profile_created_date);
    }

    private void loadUserProfile() {
        new Thread(() -> {
            try {

                SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                String cookie = prefs.getString("cookie", "");

                URL url = new URL(API_URL + "?action=profile_get");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (!cookie.isEmpty()) {
                    conn.setRequestProperty("Cookie", cookie);
                }

                int responseCode = conn.getResponseCode();
                String jsonResponse = readStream(conn.getInputStream());
                JSONObject response = new JSONObject(jsonResponse);

                if (response.getBoolean("success")) {
                    JSONObject data = response.getJSONObject("data");

                    String account = data.optString("Account", "Unknown");
                    String firstName = data.optString("FirstName", "");
                    String lastName = data.optString("LastName", "");
                    String imageUrl = data.optString("ImageURL", "");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        String displayName = (firstName + " " + lastName).trim();
                        if (displayName.isEmpty()) displayName = account;

                        profileUsername.setText(displayName);
                        profileEmail.setText(account);
                        profileCreatedDate.setText("Ngày tham gia: Chưa xác định");

                        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
                            loadImageFromUrl(imageUrl);
                        } else {
                            profileAvatar.setImageResource(R.drawable.acount_avt);
                        }
                    });

                } else {
                    showToast("Lỗi: " + response.optString("error"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Không thể tải hồ sơ", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                    String savedAccount = prefs.getString("account", "Người dùng");
                    profileUsername.setText(savedAccount);
                    profileEmail.setText(savedAccount + "@mangaapp.com");
                    profileAvatar.setImageResource(R.drawable.acount_avt);
                });
            }
        }).start();
    }

    private void loadImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                InputStream in = url.openConnection().getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (bitmap != null) {
                        profileAvatar.setImageBitmap(bitmap);
                    } else {
                        profileAvatar.setImageResource(R.drawable.acount_avt);
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        profileAvatar.setImageResource(R.drawable.acount_avt)
                );
            }
        }).start();
    }

    private String readStream(InputStream stream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show()
        );
    }
}