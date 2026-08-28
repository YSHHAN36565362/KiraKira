package com.kirakira.pilgrimage.dto;

import com.kirakira.pilgrimage.domain.Role;
import com.kirakira.pilgrimage.domain.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname(), user.getRole());
    }
}
