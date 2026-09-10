package com.sts.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sts.login.Login;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Optional<Login> findByEmail(String email);
    boolean existsByEmail(String email);
}
