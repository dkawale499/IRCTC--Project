package com.sts.Contoller;

import org.springframework.http.ResponseEntity;
import com.sts.login.*;
import com.sts.service.LoginService;

public class LoginController {
    private final LoginService loginService;
    public LoginController(LoginService loginService) { this.loginService = loginService; }
    public ResponseEntity<LoginResponse> login(LoginRequest request) { return ResponseEntity.ok(loginService.login(request)); }
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(ForgotPasswordRequest request) { return ResponseEntity.ok(loginService.requestPasswordReset(request)); }
    public ResponseEntity<String> resetPassword(ResetPasswordRequest request) { loginService.resetPassword(request); return ResponseEntity.ok("Password reset successful"); }
}
