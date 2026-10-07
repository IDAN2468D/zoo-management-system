package com.zoo.management.dto;

import com.zoo.management.model.UserRole;

public class AuthResponse {

    private boolean authenticated;
    private String username;
    private String fullName;
    private UserRole role;
    private String roleAuthority;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(boolean authenticated, String username, String fullName, UserRole role, String roleAuthority, String message) {
        this.authenticated = authenticated;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.roleAuthority = roleAuthority;
        this.message = message;
    }

    public static AuthResponse guest() {
        return new AuthResponse(false, "guest", "אורח", null, "ROLE_GUEST", "מחובר כאורח");
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getRoleAuthority() {
        return roleAuthority;
    }

    public void setRoleAuthority(String roleAuthority) {
        this.roleAuthority = roleAuthority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public java.util.List<String> getRoles() {
        if (roleAuthority != null) {
            return java.util.List.of(roleAuthority);
        }
        if (role != null) {
            return java.util.List.of(role.getSpringRole());
        }
        return java.util.List.of("ROLE_GUEST");
    }
}
