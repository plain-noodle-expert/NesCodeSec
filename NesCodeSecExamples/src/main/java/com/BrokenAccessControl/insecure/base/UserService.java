package com.uas.kelompoksatu.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.uas.kelompoksatu.user.Entities.User;
import com.uas.kelompoksatu.user.Entities.UserRole;

import jakarta.transaction.Transactional;

@EnableMethodSecurity(prePostEnabled = true)
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public User registerUser(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole(role);
        user.setLoggedIn(false);
        return userRepository.save(user);
    }

    @Transactional
    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        if (user.isLoggedIn()) {
            throw new RuntimeException("User already logged in");
        }
        user.setLoggedIn(true);
        return userRepository.save(user);
    }

    @Transactional
    public void logoutUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.isLoggedIn()) {
            user.setLoggedIn(false);
            userRepository.save(user);
        }
    }

    
}
