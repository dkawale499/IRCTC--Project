package com.sts.login;

public record LoginResponse(String token, String role) {
    public LoginResponse(String token) { this(token, "USER"); }
}
