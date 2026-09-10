package com.sts.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sts.login.*;
import com.sts.repo.LoginRepository;
import com.sts.repo.PasswordResetTokenRepository;
import com.sts.security.JwtService;

@Service
public class LoginService {
    private static final Logger logger = LogManager.getLogger(LoginService.class);
    private final LoginRepository loginRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public LoginService(LoginRepository loginRepository, PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.loginRepository = loginRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Login login = loginRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), login.getPassword())) throw new BadCredentialsException("Invalid email or password");
        String role = login.getRole() == null ? "USER" : login.getRole().toUpperCase();
        return new LoginResponse(jwtService.generateToken(email, role), role);
    }
    @Transactional
    public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest request) {
        Login login = loginRepository.findByEmail(request.email().trim().toLowerCase()).orElse(null);
        if (login == null) return new ForgotPasswordResponse("If the email is registered, a reset token has been created", null);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        resetToken.setLogin(login);
        passwordResetTokenRepository.save(resetToken);
        return new ForgotPasswordResponse("Reset token created. Send it to /auth/reset-password", resetToken.getToken());
    }
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters long");
        PasswordResetToken token = passwordResetTokenRepository.findByTokenAndUsedFalse(request.token()).orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        if (token.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Invalid or expired reset token");
        Login login = token.getLogin();
        login.setPassword(passwordEncoder.encode(request.newPassword()));
        loginRepository.save(login);
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }
}
