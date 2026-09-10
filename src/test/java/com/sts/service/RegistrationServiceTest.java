package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sts.login.Login;
import com.sts.registration.*;
import com.sts.repo.LoginRepository;
import com.sts.repo.RegistrationRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock private RegistrationRepository registrationRepository;
    @Mock private LoginRepository loginRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private RegistrationService registrationService;
    @BeforeEach void setUp() { registrationService=new RegistrationService(registrationRepository,loginRepository,passwordEncoder); }
    @Test void registerNormalizesEmailAndCreatesLinkedLogin() { RegistrationRequest request=new RegistrationRequest("Test User"," USER@EXAMPLE.COM ","1234567890","password123"); when(loginRepository.existsByEmail("user@example.com")).thenReturn(false); when(passwordEncoder.encode("password123")).thenReturn("encoded-password"); when(registrationRepository.save(any(Registration.class))).thenAnswer(i->i.getArgument(0)); Registration r=registrationService.register(request); assertEquals("user@example.com",r.getEmail()); assertEquals("Test User",r.getFullName()); assertEquals("1234567890",r.getPhone()); assertEquals("user@example.com",r.getLogin().getEmail()); assertEquals("encoded-password",r.getLogin().getPassword()); assertEquals(r,r.getLogin().getRegistration()); verify(registrationRepository).save(r); }
    @Test void registerRejectsExistingEmail() { when(loginRepository.existsByEmail("user@example.com")).thenReturn(true); assertThrows(IllegalArgumentException.class,()->registrationService.register(new RegistrationRequest("Test User","user@example.com","1234567890","password123"))); verify(registrationRepository,never()).save(any()); }
}
