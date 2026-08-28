package com.kirakira.pilgrimage.dto;

import com.kirakira.pilgrimage.domain.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long placeId,
        Long userId,
        String nickname,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getPlace().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
