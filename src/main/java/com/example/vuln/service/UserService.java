package com.example.vuln.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.example.vuln.dto.SignupForm;
import com.example.vuln.model.UserAccount;
import com.example.vuln.repo.UserRepository;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public UserAccount register(SignupForm form) {
        if (users.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("username_taken");
        }
        if (users.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("email_taken");
        }

        String salt = BCrypt.gensalt(12);
        String hash = BCrypt.hashpw(form.getPassword(), salt);

        UserAccount u = new UserAccount();
        u.setUsername(form.getUsername());
        u.setEmail(form.getEmail());
        u.setPasswordHash(hash);
        return users.save(u);
    }

    public Optional<UserAccount> findByUsername(String username) {
        return users.findByUsername(username);
    }

    public boolean verifyPassword(String rawPassword, String passwordHash) {
        return BCrypt.checkpw(rawPassword, passwordHash);
    }
}
