package com.sts.Contoller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sts.registration.RegistrationRequest;
import com.sts.service.RegistrationService;

public class RegistrationController {
    private final RegistrationService registrationService;
    public RegistrationController(RegistrationService registrationService) { this.registrationService = registrationService; }
    public ResponseEntity<String> register(RegistrationRequest request) { registrationService.register(request); return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful"); }
}
