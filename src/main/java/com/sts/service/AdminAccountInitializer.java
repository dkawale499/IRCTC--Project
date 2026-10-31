package com.sts.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sts.login.Login;
import com.sts.registration.Registration;
import com.sts.repo.LoginRepository;
import com.sts.repo.RegistrationRepository;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger logger = LogManager.getLogger(AdminAccountInitializer.class);

    private final LoginRepository loginRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminAccountInitializer(
            LoginRepository loginRepository,
            RegistrationRepository registrationRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:dkawale}") String username,
            @Value("${app.admin.password:}") String password) {
        this.loginRepository = loginRepository;
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username.trim().toLowerCase();
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(password)) {
            logger.warn("Admin bootstrap is disabled because app.admin.password is not configured");
            return;
        }
        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("Admin bootstrap username must not be blank");
        }

        Login existingLogin = loginRepository.findByEmail(username).orElse(null);
        if (existingLogin != null) {
            if (!"ADMIN".equalsIgnoreCase(existingLogin.getRole())) {
                throw new IllegalStateException(
                        "The configured admin username already belongs to a non-admin account");
            }
            logger.info("Configured admin account already exists");
            return;
        }

        Registration registration = new Registration();
        registration.setFullName("Administrator");
        registration.setEmail(username);
        registration.setPhone("");

        Login login = new Login();
        login.setEmail(username);
        login.setPassword(passwordEncoder.encode(password));
        login.setRole("ADMIN");
        login.setRegistration(registration);

        registration.setLogin(login);
        registrationRepository.save(registration);
        logger.info("Bootstrap admin account created for username={}", username);
    }
}
