package com.example.doanck.data.model;

public class LoginRequest {
    public String account;
    public String password;
    public LoginRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }
}
