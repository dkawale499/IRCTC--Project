package com.sts.Contoller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sts.login.*;
import com.sts.service.LoginService;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {
    @Mock private LoginService loginService;
    private LoginController controller;
    @BeforeEach void setUp() { controller = new LoginController(loginService); }
    @Test void loginReturnsServiceResponse() { LoginRequest request=new LoginRequest("user@example.com","password123"); LoginResponse response=new LoginResponse("jwt-token"); when(loginService.login(request)).thenReturn(response); ResponseEntity<LoginResponse> result=controller.login(request); assertEquals(HttpStatus.OK,result.getStatusCode()); assertEquals(response,result.getBody()); verify(loginService).login(request); }
    @Test void forgotPasswordReturnsServiceResponse() { ForgotPasswordRequest request=new ForgotPasswordRequest("user@example.com"); ForgotPasswordResponse response=new ForgotPasswordResponse("created","reset-token"); when(loginService.requestPasswordReset(request)).thenReturn(response); ResponseEntity<ForgotPasswordResponse> result=controller.forgotPassword(request); assertEquals(HttpStatus.OK,result.getStatusCode()); assertEquals(response,result.getBody()); verify(loginService).requestPasswordReset(request); }
    @Test void resetPasswordReturnsSuccessMessage() { ResetPasswordRequest request=new ResetPasswordRequest("reset-token","newpassword123"); ResponseEntity<String> result=controller.resetPassword(request); assertEquals(HttpStatus.OK,result.getStatusCode()); assertEquals("Password reset successful",result.getBody()); verify(loginService).resetPassword(request); }
}
