package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.exception.user.InvalidCredentialsException;
import com.tariff.backend.exception.user.UserAlreadyExistsException;
import com.tariff.backend.exception.user.UserNotFoundException;
import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
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
    void addUserShouldHashPasswordAndSave() {
        UserRequestDTO.AddUserDto request = new UserRequestDTO.AddUserDto("new@user.com", "SecurePass1");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.addUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        assertThat(persisted.getEmail()).isEqualTo(request.email());
        assertThat(persisted.getPassword()).isEqualTo("hashed");
        assertThat(savedUser).isSameAs(persisted);
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void addUserShouldThrowWhenEmailAlreadyExists() {
        UserRequestDTO.AddUserDto request = new UserRequestDTO.AddUserDto("exists@user.com", "SecurePass1");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User(request.email(), "hash")));

        assertThatThrownBy(() -> userService.addUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("A user with this email already exists.");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUserShouldThrowWhenPasswordWeak() {
        UserRequestDTO.AddUserDto request = new UserRequestDTO.AddUserDto("weak@user.com", "weak");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addUser(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password must be at least 8 characters long, contain an uppercase letter and a number.");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void loginUserShouldReturnUserWhenCredentialsValid() {
        User user = new User("login@user.com", "stored");
        UserRequestDTO.LoginDto request = new UserRequestDTO.LoginDto(user.getEmail(), "Password1");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        User result = userService.loginUser(request);

        assertThat(result).isSameAs(user);
        verify(passwordEncoder).matches(request.password(), user.getPassword());
    }

    @Test
    void loginUserShouldThrowWhenUserMissing() {
        UserRequestDTO.LoginDto request = new UserRequestDTO.LoginDto("missing@user.com", "Password1");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("We couldn't find an account with that email. Please check your details.");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginUserShouldThrowWhenPasswordInvalid() {
        User user = new User("login@user.com", "stored");
        UserRequestDTO.LoginDto request = new UserRequestDTO.LoginDto(user.getEmail(), "WrongPass1");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid password. Please try again.");
    }

    @Test
    void updatePasswordShouldPersistNewHash() {
        User user = new User("change@user.com", "stored");
        UserRequestDTO.UpdatePasswordDto request = new UserRequestDTO.UpdatePasswordDto(
            user.getEmail(), "Current1", "NewPass1");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new-hash");
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updatePassword(request);

        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(result).isSameAs(user);
        verify(passwordEncoder).encode(request.newPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePasswordShouldThrowWhenUserMissing() {
        UserRequestDTO.UpdatePasswordDto request = new UserRequestDTO.UpdatePasswordDto(
            "missing@user.com", "Current1", "NewPass1");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePassword(request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found with email: missing@user.com");
    }

    @Test
    void updatePasswordShouldThrowWhenCurrentPasswordWrong() {
        User user = new User("change@user.com", "stored");
        UserRequestDTO.UpdatePasswordDto request = new UserRequestDTO.UpdatePasswordDto(
            user.getEmail(), "Wrong1", "NewPass1");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid password. Please try again.");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordShouldThrowWhenNewPasswordWeak() {
        User user = new User("change@user.com", "stored");
        UserRequestDTO.UpdatePasswordDto request = new UserRequestDTO.UpdatePasswordDto(
            user.getEmail(), "Current1", "weak");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> userService.updatePassword(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password must be at least 8 characters long, contain an uppercase letter and a number.");
        verify(passwordEncoder, never()).encode(any());
    }
}
