package com.example.doanck.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit DTO mirroring api.php?action=profile_get responses.
 */
public class ProfileResponse {
    public boolean success;
    public ProfileData data;
    public String error;

    public static class ProfileData {
        @SerializedName("Account")
        public String account;
        @SerializedName("FirstName")
        public String firstName;
        @SerializedName("LastName")
        public String lastName;
        @SerializedName("ImageURL")
        public String imageURL;
    }
}
