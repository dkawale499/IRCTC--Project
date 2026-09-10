package com.sts.Contoller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sts.registration.RegistrationRequest;
import com.sts.service.RegistrationService;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {
    @Mock private RegistrationService registrationService;
    private RegistrationController controller;
    @BeforeEach void setUp() { controller = new RegistrationController(registrationService); }
    @Test void registerReturnsCreatedResponse() { RegistrationRequest request=new RegistrationRequest("Test User","user@example.com","1234567890","password123"); ResponseEntity<String> result=controller.register(request); assertEquals(HttpStatus.CREATED,result.getStatusCode()); assertEquals("Registration successful",result.getBody()); verify(registrationService).register(request); }
}
