package com.example.doanck.data.model;

public class ResetPasswordRequest {
    private String resetToken;
    private String newPassword;

    public ResetPasswordRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public String getResetToken() { return resetToken; }
    public String getNewPassword() { return newPassword; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

