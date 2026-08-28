package com.kirakira.pilgrimage.controller;

import com.kirakira.pilgrimage.dto.ReviewRequest;
import com.kirakira.pilgrimage.dto.ReviewResponse;
import com.kirakira.pilgrimage.security.SecurityUser;
import com.kirakira.pilgrimage.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/places/{placeId}/reviews")
    public List<ReviewResponse> listByPlace(@PathVariable Long placeId) {
        return reviewService.listByPlace(placeId);
    }

    @PostMapping("/api/places/{placeId}/reviews")
    public ResponseEntity<ReviewResponse> create(
            @PathVariable Long placeId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        ReviewResponse response = reviewService.create(principal.getUser().getId(), placeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/reviews/{reviewId}")
    public ReviewResponse update(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        return reviewService.update(reviewId, principal.getUser().getId(), request);
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId, @AuthenticationPrincipal SecurityUser principal) {
        reviewService.delete(reviewId, principal.getUser().getId(), principal.getUser().getRole());
        return ResponseEntity.noContent().build();
    }
}
