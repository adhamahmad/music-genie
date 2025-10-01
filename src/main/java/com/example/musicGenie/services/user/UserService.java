package com.example.musicGenie.services.user;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.enums.Role;
import com.example.musicGenie.models.User;
import com.example.musicGenie.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }

    @Transactional
    public User createUser(ProviderUserData userData) {
        User user = User.builder()
                        .email(userData.getEmail())
                        .displayName(userData.getDisplayName())
                        .profilePic(userData.getProfilePic())
                        .role(Role.USER)
                        .createdAt(LocalDateTime.now())
                        .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user with email: {}", userData.getEmail());
        return savedUser;
    }

    @Transactional
    public User findOrCreateUser(ProviderUserData userData) {
        return findByEmail(userData.getEmail())
                .orElseGet(() -> createUser(userData));
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public String getEmailById(Long id) {
        return userRepository.findEmailById(id)
                             .orElseThrow(() -> new IllegalStateException("User not found with id: " + id));
    }
}
