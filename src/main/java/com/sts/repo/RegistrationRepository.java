package com.sts.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import com.sts.registration.Registration;

@NoRepositoryBean
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
}
