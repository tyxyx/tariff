package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void listUsersShouldReturnAllUsers() {
        List<User> users = List.of(new User("alice@email.com", "hash"));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.listUsers();

        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    void registerNewUserShouldHashPasswordAndSave() {
        String email = "new@user.com";
        String password = "s3cret!";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerNewUser(email, password);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        assertThat(persisted.getEmail()).isEqualTo(email);
        assertThat(persisted.getPassword()).isNotEqualTo(password);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches(password, persisted.getPassword())).isTrue();
        assertThat(savedUser).isSameAs(persisted);
    }

    @Test
    void registerNewUserShouldThrowWhenEmailAlreadyExists() {
        String email = "exists@user.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(email, "hash")));

        assertThatThrownBy(() -> userService.registerNewUser(email, "password"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("A user with this email already exists.");
    }

    @Test
    void loginUserShouldReturnTrueForValidCredentials() {
        String email = "login@user.com";
        String password = "password";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(email, hashedPassword)));

        boolean result = userService.loginUser(email, password);

        assertThat(result).isTrue();
    }

    @Test
    void loginUserShouldReturnFalseForInvalidPassword() {
        String email = "login@user.com";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("correct-password");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(email, hashedPassword)));

        boolean result = userService.loginUser(email, "wrong");

        assertThat(result).isFalse();
    }

    @Test
    void loginUserShouldReturnFalseWhenUserMissing() {
        when(userRepository.findByEmail("missing@user.com")).thenReturn(Optional.empty());

        boolean result = userService.loginUser("missing@user.com", "password");

        assertThat(result).isFalse();
    }
}
