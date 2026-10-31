package com.sts.service;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.sts.login.Login;
import com.sts.registration.Registration;
import com.sts.registration.RegistrationRequest;
import com.sts.registration.RegisteredUser;
import com.sts.repo.LoginRepository;
import com.sts.repo.RegistrationRepository;
import com.sts.repo.TicketRepository;

@Service
public class RegistrationService {
    private static final Logger logger = LogManager.getLogger(RegistrationService.class);
    private final RegistrationRepository registrationRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final TicketRepository ticketRepository;
    public RegistrationService(RegistrationRepository registrationRepository, LoginRepository loginRepository,
            PasswordEncoder passwordEncoder, WalletService walletService, TicketRepository ticketRepository) {
        this.registrationRepository = registrationRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.ticketRepository = ticketRepository;
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
        walletService.addSignupBonus(savedRegistration);
        logger.info("User registration completed successfully");
        return savedRegistration;
    }

    @Transactional(readOnly = true)
    public List<RegisteredUser> getRegisteredUsers() {
        return registrationRepository.findAllByLogin_Role("USER").stream()
                .map(this::toRegisteredUser)
                .toList();
    }

    @Transactional
    public RegisteredUser createRegisteredUser(RegistrationRequest request) {
        if (request == null
                || request.fullName() == null || request.fullName().isBlank()
                || request.email() == null || !request.email().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || request.phone() == null || request.phone().isBlank()
                || request.password() == null || request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a full name, valid email, phone number, and password with at least 8 characters.");
        }

        try {
            return toRegisteredUser(register(request));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    @Transactional
    public void deleteRegisteredUser(Long userId) {
        Registration registration = registrationRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registered user not found."));
        if (registration.getLogin() == null || !"USER".equalsIgnoreCase(registration.getLogin().getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only passenger accounts can be deleted here.");
        }
        ticketRepository.findAllByRegistration_Id(userId).forEach(ticket -> ticket.setRegistration(null));
        ticketRepository.flush();
        registrationRepository.delete(registration);
        logger.info("Passenger account deleted by an administrator");
    }

    private RegisteredUser toRegisteredUser(Registration registration) {
        return new RegisteredUser(
                registration.getId(),
                registration.getFullName(),
                registration.getEmail(),
                registration.getPhone());
    }
}
