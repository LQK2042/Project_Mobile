package com.example.doanck.core.utils;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class ApiErrorUtils {
    public static GenericError parseError(Response<?> response) {
        if (response == null || response.errorBody() == null) return null;
        ResponseBody errorBody = response.errorBody();
        try {
            String errString = errorBody.string();
            Gson gson = new Gson();
            return gson.fromJson(errString, GenericError.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class GenericError {
        public boolean success;
        public String message;
        public String error;
    }
}

