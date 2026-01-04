package com.example.doanck.data.model;

public class LoginResponse {
    // Tên các thuộc tính phải khớp với key trong JSON trả về từ API// Ví dụ: {"status": "success", "message": "Login successful", "token": "abc..."}
    public String status;
    public String message;
    public String token; // Hoặc tên khác tùy vào API của bạn
}
