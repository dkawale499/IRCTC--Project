package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import com.sts.login.Login;
import com.sts.registration.*;
import com.sts.repo.LoginRepository;
import com.sts.repo.RegistrationRepository;
import com.sts.repo.TicketRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock private RegistrationRepository registrationRepository;
    @Mock private LoginRepository loginRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private WalletService walletService;
    @Mock private TicketRepository ticketRepository;
    private RegistrationService registrationService;
    @BeforeEach void setUp() { registrationService=new RegistrationService(registrationRepository,loginRepository,passwordEncoder,walletService,ticketRepository); }
    @Test void registerNormalizesEmailAndCreatesLinkedLogin() { RegistrationRequest request=new RegistrationRequest("Test User"," USER@EXAMPLE.COM ","1234567890","password123"); when(loginRepository.existsByEmail("user@example.com")).thenReturn(false); when(passwordEncoder.encode("password123")).thenReturn("encoded-password"); when(registrationRepository.save(any(Registration.class))).thenAnswer(i->i.getArgument(0)); Registration r=registrationService.register(request); assertEquals("user@example.com",r.getEmail()); assertEquals("Test User",r.getFullName()); assertEquals("1234567890",r.getPhone()); assertEquals("user@example.com",r.getLogin().getEmail()); assertEquals("encoded-password",r.getLogin().getPassword()); assertEquals(r,r.getLogin().getRegistration()); verify(registrationRepository).save(r); verify(walletService).addSignupBonus(r); }
    @Test void registerRejectsExistingEmail() { when(loginRepository.existsByEmail("user@example.com")).thenReturn(true); assertThrows(IllegalArgumentException.class,()->registrationService.register(new RegistrationRequest("Test User","user@example.com","1234567890","password123"))); verify(registrationRepository,never()).save(any()); }
    @Test void getRegisteredUsersReturnsUserProfilesWithoutCredentials() {
        Registration registration = new Registration();
        registration.setId(5L);
        registration.setFullName("Test User");
        registration.setEmail("user@example.com");
        registration.setPhone("1234567890");
        when(registrationRepository.findAllByLogin_Role("USER")).thenReturn(java.util.List.of(registration));
        assertEquals(java.util.List.of(new RegisteredUser(5L, "Test User", "user@example.com", "1234567890")),
                registrationService.getRegisteredUsers());
    }
    @Test void createRegisteredUserRejectsInvalidInput() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> registrationService.createRegisteredUser(new RegistrationRequest("", "bad-email", "", "short")));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(registrationRepository, never()).save(any());
    }
    @Test void deleteRegisteredUserDeletesPassengerRegistration() {
        Registration registration = new Registration();
        Login login = new Login();
        login.setRole("USER");
        registration.setLogin(login);
        when(registrationRepository.findById(5L)).thenReturn(java.util.Optional.of(registration));
        when(ticketRepository.findAllByRegistration_Id(5L)).thenReturn(java.util.List.of());
        registrationService.deleteRegisteredUser(5L);
        verify(ticketRepository).flush();
        verify(registrationRepository).delete(registration);
    }
    @Test void deleteRegisteredUserRejectsAdminAccounts() {
        Registration registration = new Registration();
        Login login = new Login();
        login.setRole("ADMIN");
        registration.setLogin(login);
        when(registrationRepository.findById(5L)).thenReturn(java.util.Optional.of(registration));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> registrationService.deleteRegisteredUser(5L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(registrationRepository, never()).delete(any(Registration.class));
    }
}
