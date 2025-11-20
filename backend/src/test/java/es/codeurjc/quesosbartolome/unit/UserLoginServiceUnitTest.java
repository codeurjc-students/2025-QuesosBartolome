package es.codeurjc.quesosbartolome.unit;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import es.codeurjc.quesosbartolome.security.jwt.AuthResponse.Status;
import es.codeurjc.quesosbartolome.security.jwt.AuthResponse;
import es.codeurjc.quesosbartolome.security.jwt.JwtTokenProvider;
import es.codeurjc.quesosbartolome.security.jwt.LoginRequest;
import es.codeurjc.quesosbartolome.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

public class UserLoginServiceUnitTest {

    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private JwtTokenProvider jwtTokenProvider;
    private UserLoginService loginService;

    @BeforeEach
    void setup() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);

        loginService = new UserLoginService(authenticationManager, userDetailsService, jwtTokenProvider);
    }

    @Test
    void loginShouldReturnSuccessAndSetCookies() {

        LoginRequest request = new LoginRequest("john", "password");
        HttpServletResponse response = mock(HttpServletResponse.class);

        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        var userDetails = mock(org.springframework.security.core.userdetails.User.class);

        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);

        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access123");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh456");

        ResponseEntity<AuthResponse> result = loginService.login(response, request);

        assertThat(result.getBody().getStatus()).isEqualTo(Status.SUCCESS);
        verify(response, times(2)).addCookie(any());
    }
}
