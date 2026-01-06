package com.example.doanck.core.network;

import com.example.doanck.core.utils.SessionManager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import java.io.IOException;
import java.util.List;

public class ApiClient {

    private static final String BASE_URL = ""; // TODO: change to real URL
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new CookieInterceptor())
                    .addInterceptor(new ReceivedCookieInterceptor())
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static class CookieInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            String cookie = SessionManager.getCookie();
            if (cookie != null && !cookie.isEmpty()) {
                builder.addHeader("Cookie", cookie);
            }
            return chain.proceed(builder.build());
        }
    }

    private static class ReceivedCookieInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            List<String> cookies = response.headers("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                StringBuilder combinedCookies = new StringBuilder();
                boolean hasSessionCookie = false;
                for (String header : cookies) {
                    if (header == null || header.isEmpty()) {
                        continue;
                    }
                    String trimmed = header.split(";", 2)[0].trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    // Chỉ quan tâm đến PHPSESSID
                    if (trimmed.startsWith("PHPSESSID=")) {
                        hasSessionCookie = true;
                    }
                    if (combinedCookies.length() > 0) {
                        combinedCookies.append("; ");
                    }
                    combinedCookies.append(trimmed);
                }
                // Chỉ lưu cookie nếu có PHPSESSID - tránh ghi đè session từ các API không cần auth
                if (hasSessionCookie && combinedCookies.length() > 0) {
                    SessionManager.saveCookie(combinedCookies.toString());
                }
            }
            return response;
        }
    }

    public static CategoryApi getCategoryApi() {
        return getClient().create(CategoryApi.class);
    }

    public static ProfileApi getProfileApi() {
        return getClient().create(ProfileApi.class);
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
//end
