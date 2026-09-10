package com.sts.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock private JwtService jwtService; @Mock private HttpServletRequest request; @Mock private HttpServletResponse response; @Mock private FilterChain filterChain;
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void validBearerTokenSetsAuthentication() throws ServletException, IOException { when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token"); when(jwtService.extractEmail("jwt-token")).thenReturn("user@example.com"); when(jwtService.extractRole("jwt-token")).thenReturn("ADMIN"); new JwtAuthenticationFilter(jwtService).doFilterInternal(request,response,filterChain); assertEquals("user@example.com",SecurityContextHolder.getContext().getAuthentication().getPrincipal()); assertEquals("ROLE_ADMIN",SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority()); verify(filterChain).doFilter(request,response); }
    @Test void invalidBearerTokenLeavesRequestUnauthenticated() throws ServletException, IOException { when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token"); when(jwtService.extractEmail("invalid-token")).thenThrow(new IllegalArgumentException("invalid token")); new JwtAuthenticationFilter(jwtService).doFilterInternal(request,response,filterChain); assertNull(SecurityContextHolder.getContext().getAuthentication()); verify(filterChain).doFilter(request,response); }
    @Test void requestWithoutAuthorizationHeaderPassesThrough() throws ServletException, IOException { when(request.getHeader("Authorization")).thenReturn(null); new JwtAuthenticationFilter(jwtService).doFilterInternal(request,response,filterChain); assertNull(SecurityContextHolder.getContext().getAuthentication()); verify(filterChain).doFilter(request,response); }
}
