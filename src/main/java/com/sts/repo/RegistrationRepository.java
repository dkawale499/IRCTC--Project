package com.sts.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sts.registration.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
}
