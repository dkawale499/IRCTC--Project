package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sts.login.ForgotPasswordRequest;
import com.sts.login.ForgotPasswordResponse;
import com.sts.login.Login;
import com.sts.login.LoginRequest;
import com.sts.login.PasswordResetToken;
import com.sts.login.ResetPasswordRequest;
import com.sts.repo.LoginRepository;
import com.sts.repo.PasswordResetTokenRepository;
import com.sts.security.JwtService;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock private LoginRepository loginRepository; @Mock private PasswordResetTokenRepository tokenRepository; @Mock private PasswordEncoder passwordEncoder; @Mock private JwtService jwtService;
    private LoginService service;
    @BeforeEach void setUp(){service=new LoginService(loginRepository,tokenRepository,passwordEncoder,jwtService);}
    @Test void loginReturnsTokenForValidCredentials(){ Login login=new Login(); login.setEmail("user@example.com"); login.setPassword("encoded-password"); when(loginRepository.findByEmail("user@example.com")).thenReturn(Optional.of(login)); when(passwordEncoder.matches("password123","encoded-password")).thenReturn(true); when(jwtService.generateToken("user@example.com","USER")).thenReturn("jwt-token"); assertEquals("jwt-token",service.login(new LoginRequest(" USER@EXAMPLE.COM ","password123")).token()); }
    @Test void loginRejectsUnknownEmail(){when(loginRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty()); assertThrows(BadCredentialsException.class,()->service.login(new LoginRequest("missing@example.com","password123"))); verify(passwordEncoder,never()).matches(any(),any());}
    @Test void loginRejectsIncorrectPassword(){Login login=new Login(); login.setPassword("encoded-password"); when(loginRepository.findByEmail("user@example.com")).thenReturn(Optional.of(login)); when(passwordEncoder.matches("wrong-password","encoded-password")).thenReturn(false); assertThrows(BadCredentialsException.class,()->service.login(new LoginRequest("user@example.com","wrong-password")));}
    @Test void forgotPasswordCreatesToken(){Login login=new Login(); when(loginRepository.findByEmail("user@example.com")).thenReturn(Optional.of(login)); ForgotPasswordResponse response=service.requestPasswordReset(new ForgotPasswordRequest("USER@EXAMPLE.COM")); assertNotNull(response.resetToken()); verify(tokenRepository).save(any(PasswordResetToken.class));}
    @Test void forgotPasswordDoesNotRevealUnknownEmail(){when(loginRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty()); assertNull(service.requestPasswordReset(new ForgotPasswordRequest("missing@example.com")).resetToken()); verify(tokenRepository,never()).save(any());}
    @Test void resetPasswordUpdatesPassword(){Login login=new Login(); PasswordResetToken token=new PasswordResetToken(); token.setLogin(login); token.setExpiresAt(Instant.now().plusSeconds(300)); when(tokenRepository.findByTokenAndUsedFalse("reset-token")).thenReturn(Optional.of(token)); when(passwordEncoder.encode("newpassword123")).thenReturn("encoded-new-password"); service.resetPassword(new ResetPasswordRequest("reset-token","newpassword123")); assertEquals("encoded-new-password",login.getPassword()); assertTrue(token.isUsed()); verify(loginRepository).save(login); verify(tokenRepository).save(token);}
    @Test void resetPasswordRejectsShortPassword(){assertThrows(IllegalArgumentException.class,()->service.resetPassword(new ResetPasswordRequest("reset-token","short"))); verify(tokenRepository,never()).findByTokenAndUsedFalse(any());}
}
