package com.kirakira.pilgrimage.service;

import com.kirakira.pilgrimage.domain.Favorite;
import com.kirakira.pilgrimage.domain.Place;
import com.kirakira.pilgrimage.domain.User;
import com.kirakira.pilgrimage.dto.FavoriteResponse;
import com.kirakira.pilgrimage.dto.PlaceResponse;
import com.kirakira.pilgrimage.exception.FavoriteAlreadyExistsException;
import com.kirakira.pilgrimage.exception.FavoriteNotFoundException;
import com.kirakira.pilgrimage.exception.PlaceNotFoundException;
import com.kirakira.pilgrimage.repository.FavoriteRepository;
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
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void add(Long userId, Long placeId) {
        if (favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            throw new FavoriteAlreadyExistsException();
        }
        User user = userRepository.getReferenceById(userId);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException(placeId));
        favoriteRepository.save(new Favorite(user, place));
    }

    @Transactional
    public void remove(Long userId, Long placeId) {
        Favorite favorite = favoriteRepository.findByUserIdAndPlaceId(userId, placeId)
                .orElseThrow(FavoriteNotFoundException::new);
        favoriteRepository.delete(favorite);
    }

    public List<FavoriteResponse> list(Long userId) {
        return favoriteRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(favorite -> {
                    Place place = favorite.getPlace();
                    ReviewRepository.RatingSummary summary = reviewRepository.summarizeByPlaceId(place.getId());
                    PlaceResponse placeResponse = PlaceResponse.from(place, summary.getAverageRating(), summary.getReviewCount());
                    return FavoriteResponse.from(favorite, placeResponse);
                })
                .toList();
    }
}
