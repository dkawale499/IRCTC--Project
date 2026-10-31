package com.sts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sts.login.Login;
import com.sts.registration.Registration;
import com.sts.repo.LoginRepository;
import com.sts.repo.RegistrationRepository;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {
    @Mock private LoginRepository loginRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void createsAdminAccountWithEncodedPasswordWhenConfigured() {
        AdminAccountInitializer initializer = new AdminAccountInitializer(
                loginRepository, registrationRepository, passwordEncoder, "DKAWALE", "local-password");
        when(loginRepository.findByEmail("dkawale")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("local-password")).thenReturn("encoded-password");

        initializer.run(null);

        ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).save(registrationCaptor.capture());
        Registration savedRegistration = registrationCaptor.getValue();
        assertEquals("dkawale", savedRegistration.getEmail());
        assertEquals("dkawale", savedRegistration.getLogin().getEmail());
        assertEquals("ADMIN", savedRegistration.getLogin().getRole());
        assertEquals("encoded-password", savedRegistration.getLogin().getPassword());
        assertEquals(savedRegistration, savedRegistration.getLogin().getRegistration());
    }

    @Test
    void doesNotCreateAccountWhenPasswordIsNotConfigured() {
        AdminAccountInitializer initializer = new AdminAccountInitializer(
                loginRepository, registrationRepository, passwordEncoder, "dkawale", " ");

        initializer.run(null);

        verifyNoInteractions(loginRepository, registrationRepository, passwordEncoder);
    }

    @Test
    void doesNotOverwriteExistingAdminAccount() {
        Login existingAdmin = new Login();
        existingAdmin.setRole("ADMIN");
        AdminAccountInitializer initializer = new AdminAccountInitializer(
                loginRepository, registrationRepository, passwordEncoder, "dkawale", "local-password");
        when(loginRepository.findByEmail("dkawale")).thenReturn(Optional.of(existingAdmin));

        initializer.run(null);

        verify(registrationRepository, never()).save(any(Registration.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void refusesToPromoteExistingNonAdminAccount() {
        Login existingUser = new Login();
        existingUser.setRole("USER");
        AdminAccountInitializer initializer = new AdminAccountInitializer(
                loginRepository, registrationRepository, passwordEncoder, "dkawale", "local-password");
        when(loginRepository.findByEmail("dkawale")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalStateException.class, () -> initializer.run(null));

        verify(registrationRepository, never()).save(any(Registration.class));
        verifyNoInteractions(passwordEncoder);
    }
}
