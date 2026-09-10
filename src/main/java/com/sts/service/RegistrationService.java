package com.sts.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sts.login.Login;
import com.sts.registration.Registration;
import com.sts.registration.RegistrationRequest;
import com.sts.repo.LoginRepository;
import com.sts.repo.RegistrationRepository;

@Service
public class RegistrationService {
    private static final Logger logger = LogManager.getLogger(RegistrationService.class);
    private final RegistrationRepository registrationRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    public RegistrationService(RegistrationRepository registrationRepository, LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.registrationRepository = registrationRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public Registration register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase();
        if (loginRepository.existsByEmail(email)) {
            logger.warn("Registration rejected because the account already exists");
            throw new IllegalArgumentException("Email is already registered");
        }
        Registration registration = new Registration();
        registration.setFullName(request.fullName());
        registration.setEmail(email);
        registration.setPhone(request.phone());
        Login login = new Login();
        login.setEmail(email);
        login.setPassword(passwordEncoder.encode(request.password()));
        login.setRole("USER");
        login.setRegistration(registration);
        registration.setLogin(login);
        Registration savedRegistration = registrationRepository.save(registration);
        logger.info("User registration completed successfully");
        return savedRegistration;
    }
}
