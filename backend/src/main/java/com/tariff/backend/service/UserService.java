package com.tariff.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  public List<User> listUsers() {
    return userRepository.findAll();
  }

  // public void deleteUser(long id) {
  //   boolean exist = userRepository.existsById(id);
  //   if (!exist) {
  //     throw new IllegalStateException("User not exists");
  //   }
  //   userRepository.deleteById(id);
  // }

   public User registerNewUser(String email, String password) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalStateException("A user with this email already exists.");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);
        
        // Create and save the new user
        User newUser = new User(email, hashedPassword);
        return userRepository.save(newUser);
    }

    public boolean loginUser(String email, String password) {
      Optional<User> userOptional = userRepository.findByEmail(email);

      if (userOptional.isPresent()) {
        User user = userOptional.get();
        return passwordEncoder.matches(password, user.getPassword());
      }

      return false;
    }
    
    public boolean changePassword(String email, String password, String newPassword) {
      Optional<User> userOptional = userRepository.findByEmail(email);

      if (userOptional.isPresent()) {
        User user = userOptional.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
          String hashedNewPassword = passwordEncoder.encode(newPassword);
          user.setPassword(hashedNewPassword);
          userRepository.save(user);  
          return true;
        }
      }

      return false;
    }
}
