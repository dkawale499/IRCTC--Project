package com.sts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest
class IrctcProjectApplicationTests {

	@Autowired
	private SecurityFilterChain securityFilterChain;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	void securityConfigurationProvidesRequiredBeans() {
		org.junit.jupiter.api.Assertions.assertNotNull(securityFilterChain);
		org.junit.jupiter.api.Assertions.assertNotNull(passwordEncoder);
	}

}
