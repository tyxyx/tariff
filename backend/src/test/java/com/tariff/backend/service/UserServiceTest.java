package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.exception.InvalidCredentialsException;
import com.tariff.backend.exception.UserAlreadyExistsException;
import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordEncoder, authenticationManager);
    // Set the @Value fields manually since we're using mocks
    ReflectionTestUtils.setField(userService, "minPwdLength", 8);
    ReflectionTestUtils.setField(userService, "maxPwdLength", 16);
  }

  @Test
  void listUsersShouldReturnAllUsers() {
    List<User> users = List.of(new User("alice@email.com", "hash", null));
    when(userRepository.findAll()).thenReturn(users);

    List<User> result = userService.listUsers();

    assertThat(result).isEqualTo(users);
    verify(userRepository).findAll();
  }

  @Test
  void addUserShouldHashPasswordAndSave() {
    UserRequestDTO.AddUserDto request = new UserRequestDTO.AddUserDto(
      "new@user.com",
      "SecurePass1"
    );
    when(userRepository.findByEmail(request.email())).thenReturn(
      Optional.empty()
    );
    when(passwordEncoder.encode(request.password())).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation ->
      invocation.getArgument(0)
    );

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
    UserRequestDTO.AddUserDto request = new UserRequestDTO.AddUserDto(
      "exists@user.com",
      "SecurePass1"
    );
    when(userRepository.findByEmail(request.email())).thenReturn(
      Optional.of(new User(request.email(), "hash", null))
    );

    assertThatThrownBy(() -> userService.addUser(request))
      .isInstanceOf(UserAlreadyExistsException.class)
      .hasMessage("A user with this email already exists.");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void addUserShouldThrowWhenPasswordWeak() {
    UserRequestDTO.AddUserDto request = new UserRequestDTO.AddUserDto(
      "weak@user.com",
      "weak"
    );
    when(userRepository.findByEmail(request.email())).thenReturn(
      Optional.empty()
    );

    assertThatThrownBy(() -> userService.addUser(request))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Password must be between")
      .hasMessageContaining("characters long");
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void loginUserShouldReturnUserWhenCredentialsValid() {
    User user = new User("login@user.com", "stored", null);
    UserRequestDTO.LoginDto request = new UserRequestDTO.LoginDto(
      user.getEmail(),
      "Password1"
    );
    when(userRepository.findByEmail(user.getEmail())).thenReturn(
      Optional.of(user)
    );
    when(
      passwordEncoder.matches(request.password(), user.getPassword())
    ).thenReturn(true);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
      .thenReturn(null);

    User result = userService.loginUser(request);

    assertThat(result).isSameAs(user);
    verify(passwordEncoder).matches(request.password(), user.getPassword());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void loginUserShouldThrowWhenUserMissing() {
    UserRequestDTO.LoginDto request = new UserRequestDTO.LoginDto(
      "missing@user.com",
      "Password1"
    );
    when(userRepository.findByEmail(request.email())).thenReturn(
      Optional.empty()
    );

    assertThatThrownBy(() -> userService.loginUser(request))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessageContaining("User not found with email");
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  void loginUserShouldThrowWhenPasswordInvalid() {
    User user = new User("login@user.com", "stored", null);
    UserRequestDTO.LoginDto request = new UserRequestDTO.LoginDto(
      user.getEmail(),
      "WrongPass1"
    );
    when(userRepository.findByEmail(user.getEmail())).thenReturn(
      Optional.of(user)
    );
    when(
      passwordEncoder.matches(request.password(), user.getPassword())
    ).thenReturn(false);

    assertThatThrownBy(() -> userService.loginUser(request))
      .isInstanceOf(InvalidCredentialsException.class)
      .hasMessage("Invalid password. Please try again.");
  }

  @Test
  void updatePasswordShouldPersistNewHash() {
    User user = new User("change@user.com", "stored", null);
    String authenticatedEmail = user.getEmail();
    UserRequestDTO.UpdatePasswordDto request =
      new UserRequestDTO.UpdatePasswordDto(
        "Current1",
        "NewPass1"
      );
    when(userRepository.findByEmail(authenticatedEmail)).thenReturn(
      Optional.of(user)
    );
    when(
      passwordEncoder.matches(request.password(), user.getPassword())
    ).thenReturn(true);
    when(passwordEncoder.encode(request.newPassword())).thenReturn("new-hash");
    when(userRepository.save(user)).thenAnswer(invocation ->
      invocation.getArgument(0)
    );

    User result = userService.updatePassword(authenticatedEmail, request);

    assertThat(user.getPassword()).isEqualTo("new-hash");
    assertThat(result).isSameAs(user);
    verify(passwordEncoder).encode(request.newPassword());
    verify(userRepository).save(user);
  }

  @Test
  void updatePasswordShouldThrowWhenUserMissing() {
    String authenticatedEmail = "missing@user.com";
    UserRequestDTO.UpdatePasswordDto request =
      new UserRequestDTO.UpdatePasswordDto(
        "Current1",
        "NewPass1"
      );
    when(userRepository.findByEmail(authenticatedEmail)).thenReturn(
      Optional.empty()
    );

    assertThatThrownBy(() -> userService.updatePassword(authenticatedEmail, request))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessageContaining("User not found with email");
  }

  @Test
  void updatePasswordShouldThrowWhenCurrentPasswordWrong() {
    User user = new User("change@user.com", "stored", null);
    String authenticatedEmail = user.getEmail();
    UserRequestDTO.UpdatePasswordDto request =
      new UserRequestDTO.UpdatePasswordDto(
        "Wrong1",
        "NewPass1"
      );
    when(userRepository.findByEmail(authenticatedEmail)).thenReturn(
      Optional.of(user)
    );
    when(
      passwordEncoder.matches(request.password(), user.getPassword())
    ).thenReturn(false);

    assertThatThrownBy(() -> userService.updatePassword(authenticatedEmail, request))
      .isInstanceOf(InvalidCredentialsException.class)
      .hasMessage("Invalid password. Please try again.");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updatePasswordShouldThrowWhenNewPasswordWeak() {
    User user = new User("change@user.com", "stored", null);
    String authenticatedEmail = user.getEmail();
    UserRequestDTO.UpdatePasswordDto request =
      new UserRequestDTO.UpdatePasswordDto("Current1", "weak");
    when(userRepository.findByEmail(authenticatedEmail)).thenReturn(
      Optional.of(user)
    );
    when(
      passwordEncoder.matches(request.password(), user.getPassword())
    ).thenReturn(true);

    assertThatThrownBy(() -> userService.updatePassword(authenticatedEmail, request))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Password must be between")
      .hasMessageContaining("characters long");
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void deleteUserShouldRemoveUser() {
    User user = new User("delete@user.com", "hash", null);
    UserRequestDTO.DeleteUserDto request = new UserRequestDTO.DeleteUserDto(user.getEmail());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    User result = userService.deleteUser(request);

    assertThat(result).isSameAs(user);
    verify(userRepository).delete(user);
  }

  @Test
  void upgradeRoleShouldPromoteUser() {
    User user = new User("upgrade@user.com", "hash", User.Role.USER);
    UserRequestDTO.UpdateUserRoleDto request = new UserRequestDTO.UpdateUserRoleDto(user.getEmail());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.upgradeRole(request);

    assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
    verify(userRepository).save(user);
  }

  @Test
  void downgradeRoleShouldDemoteUser() {
    User user = new User("downgrade@user.com", "hash", User.Role.ADMIN);
    UserRequestDTO.UpdateUserRoleDto request = new UserRequestDTO.UpdateUserRoleDto(user.getEmail());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.downgradeRole(request);

    assertThat(result.getRole()).isEqualTo(User.Role.USER);
    verify(userRepository).save(user);
  }
}
