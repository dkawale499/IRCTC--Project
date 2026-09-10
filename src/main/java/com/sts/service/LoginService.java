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
        logger.debug("Login attempt received");
        Login login = loginRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("Login rejected because the account was not found");
            return new BadCredentialsException("Invalid email or password");
        });
        if (!passwordEncoder.matches(request.password(), login.getPassword())) {
            logger.warn("Login rejected because credentials were invalid");
            throw new BadCredentialsException("Invalid email or password");
        }
        String role = login.getRole() == null ? "USER" : login.getRole().toUpperCase();
        logger.info("Login successful for role={}", role);
        return new LoginResponse(jwtService.generateToken(email, role), role);
    }
    @Transactional
    public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest request) {
        Login login = loginRepository.findByEmail(request.email().trim().toLowerCase()).orElse(null);
        if (login == null) {
            logger.info("Password reset requested for an unknown account");
            return new ForgotPasswordResponse("If the email is registered, a reset token has been created", null);
        }
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        resetToken.setLogin(login);
        passwordResetTokenRepository.save(resetToken);
        logger.info("Password reset token created");
        return new ForgotPasswordResponse("Reset token created. Send it to /auth/reset-password", resetToken.getToken());
    }
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 8) {
            logger.warn("Password reset rejected because the new password was too short");
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        PasswordResetToken token = passwordResetTokenRepository.findByTokenAndUsedFalse(request.token()).orElseThrow(() -> {
            logger.warn("Password reset rejected because the token was invalid");
            return new IllegalArgumentException("Invalid or expired reset token");
        });
        if (token.getExpiresAt().isBefore(Instant.now())) {
            logger.warn("Password reset rejected because the token was expired");
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
        Login login = token.getLogin();
        login.setPassword(passwordEncoder.encode(request.newPassword()));
        loginRepository.save(login);
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        logger.info("Password reset completed successfully");
    }
}
