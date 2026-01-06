package com.example.doanck.data.model;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("resetToken")
    private String resetToken;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getResetToken() { return resetToken; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
}

