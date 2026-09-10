package com.sts.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sts.login.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
}
