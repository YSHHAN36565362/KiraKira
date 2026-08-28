package com.kirakira.pilgrimage.service;

import com.kirakira.pilgrimage.domain.Place;
import com.kirakira.pilgrimage.domain.Review;
import com.kirakira.pilgrimage.domain.Role;
import com.kirakira.pilgrimage.domain.User;
import com.kirakira.pilgrimage.dto.ReviewRequest;
import com.kirakira.pilgrimage.dto.ReviewResponse;
import com.kirakira.pilgrimage.exception.DuplicateReviewException;
import com.kirakira.pilgrimage.exception.ForbiddenException;
import com.kirakira.pilgrimage.exception.PlaceNotFoundException;
import com.kirakira.pilgrimage.exception.ReviewNotFoundException;
import com.kirakira.pilgrimage.repository.PlaceRepository;
import com.kirakira.pilgrimage.repository.ReviewRepository;
import com.kirakira.pilgrimage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public List<ReviewResponse> listByPlace(Long placeId) {
        return reviewRepository.findByPlaceIdOrderByIdDesc(placeId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse create(Long userId, Long placeId, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            throw new DuplicateReviewException();
        }
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException(placeId));
        User user = userRepository.getReferenceById(userId);

        Review review = new Review(user, place, request.rating(), request.content());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse update(Long reviewId, Long userId, ReviewRequest request) {
        Review review = findReviewOrThrow(reviewId);
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }
        review.update(request.rating(), request.content());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void delete(Long reviewId, Long userId, Role role) {
        Review review = findReviewOrThrow(reviewId);
        boolean isOwner = review.getUser().getId().equals(userId);
        boolean isAdmin = role == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    private Review findReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }
}
