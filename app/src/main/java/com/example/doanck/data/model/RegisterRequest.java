package com.example.doanck.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("account")
    private String account;

    @SerializedName("password")
    private String password;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("imageUrl")
    private String imageUrl;

    public RegisterRequest(String account, String password, String firstName, String lastName, String imageUrl) {
        this.account = account;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
    }

    // getters (nếu cần)
}
