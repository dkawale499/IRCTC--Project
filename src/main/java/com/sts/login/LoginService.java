package com.sts.login;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.sts.security.JwtService;

@Service
public class LoginService {
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public LoginService(LoginRepository loginRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Login login = loginRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), login.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return new LoginResponse(jwtService.generateToken(email, login.getRole()), login.getRole());
    }
}
