package com.kirakira.pilgrimage.service;

import com.kirakira.pilgrimage.domain.Role;
import com.kirakira.pilgrimage.domain.User;
import com.kirakira.pilgrimage.dto.SignupRequest;
import com.kirakira.pilgrimage.dto.UserResponse;
import com.kirakira.pilgrimage.exception.EmailAlreadyExistsException;
import com.kirakira.pilgrimage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                Role.USER
        );
        return UserResponse.from(userRepository.save(user));
    }
}
