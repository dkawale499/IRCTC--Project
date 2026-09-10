package com.sts.Contoller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sts.registration.RegistrationRequest;
import com.sts.service.RegistrationService;

@RestController
@RequestMapping("/auth")
public class RegistrationController {
    private final RegistrationService registrationService;
    public RegistrationController(RegistrationService registrationService) { this.registrationService = registrationService; }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) { registrationService.register(request); return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful"); }
}
