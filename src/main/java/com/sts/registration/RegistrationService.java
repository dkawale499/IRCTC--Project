package com.sts.registration;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sts.login.Login;
import com.sts.login.LoginRepository;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    public RegistrationService(RegistrationRepository registrationRepository, LoginRepository loginRepository,
            PasswordEncoder passwordEncoder) {
        this.registrationRepository = registrationRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public Registration register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase();
        if (loginRepository.existsByEmail(email)) throw new IllegalArgumentException("Email is already registered");
        Registration registration = new Registration();
        registration.setFullName(request.fullName());
        registration.setEmail(email);
        registration.setPhone(request.phone());
        Login login = new Login();
        login.setEmail(email);
        login.setPassword(passwordEncoder.encode(request.password()));
        login.setRegistration(registration);
        registration.setLogin(login);
        return registrationRepository.save(registration);
    }
}
