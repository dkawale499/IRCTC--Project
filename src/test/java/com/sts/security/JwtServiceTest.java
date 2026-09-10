package com.sts.security;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "irctc-project-test-secret-key-1234567890";
    @Test void generatedTokenContainsEmail() { JwtService service=new JwtService(SECRET,3_600_000); String token=service.generateToken("user@example.com","ADMIN"); assertEquals("user@example.com",service.extractEmail(token)); assertEquals("ADMIN",service.extractRole(token)); }
    @Test void invalidTokenIsRejected() { JwtService service=new JwtService(SECRET,3_600_000); assertThrows(RuntimeException.class,()->service.extractEmail("invalid-token")); }
}
