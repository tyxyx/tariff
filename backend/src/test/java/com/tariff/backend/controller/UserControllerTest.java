package com.tariff.backend.controller;

import com.tariff.backend.service.JwtService;
import com.tariff.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse response;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService, jwtService);
    }

    @Test
    void logout_shouldClearAuthTokenCookie() {
        // Act
        ResponseEntity<String> result = userController.logout(response);

        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo("Logged out successfully");

        // Verify cookie was added to response
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertThat(cookie.getName()).isEqualTo("auth_token");
        assertThat(cookie.getValue()).isNull();
        assertThat(cookie.getMaxAge()).isEqualTo(0);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isFalse();
    }
}
