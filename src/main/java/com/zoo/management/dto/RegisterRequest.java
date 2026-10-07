package com.zoo.management.dto;

import com.zoo.management.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "שם משתמש הוא שדה חובה")
    @Size(min = 3, max = 50, message = "שם משתמש חייב להכיל בין 3 ל-50 תווים")
    private String username;

    @NotBlank(message = "סיסמה היא שדה חובה")
    @Size(min = 4, message = "סיסמה חייבת להכיל לפחות 4 תווים")
    private String password;

    @NotBlank(message = "שם מלא הוא שדה חובה")
    private String fullName;

    private UserRole role = UserRole.KEEPER;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String fullName, UserRole role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role != null ? role : UserRole.KEEPER;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
