package com.sts.login;

public record ResetPasswordRequest(String token, String newPassword) {
}
