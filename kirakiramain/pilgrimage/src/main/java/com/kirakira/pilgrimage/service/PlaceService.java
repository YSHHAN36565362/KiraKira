package com.kirakira.pilgrimage.service;

import com.kirakira.pilgrimage.domain.MediaType;
import com.kirakira.pilgrimage.domain.Place;
import com.kirakira.pilgrimage.dto.PlaceRequest;
import com.kirakira.pilgrimage.dto.PlaceResponse;
import com.kirakira.pilgrimage.exception.PlaceNotFoundException;
import com.kirakira.pilgrimage.repository.PlaceRepository;
import com.kirakira.pilgrimage.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;

    public List<PlaceResponse> search(MediaType mediaType, String keyword) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return placeRepository.search(mediaType, normalizedKeyword).stream()
                .map(this::toResponse)
                .toList();
    }

    public PlaceResponse getOne(Long id) {
        return toResponse(findPlaceOrThrow(id));
    }

    @Transactional
    public PlaceResponse create(PlaceRequest request) {
        Place place = new Place(
                request.mediaType(),
                request.workTitle(),
                request.placeName(),
                request.region(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.description(),
                request.animeImageUrl(),
                request.realImageUrl()
        );
        return toResponse(placeRepository.save(place));
    }

    @Transactional
    public PlaceResponse update(Long id, PlaceRequest request) {
        Place place = findPlaceOrThrow(id);
        place.update(
                request.mediaType(),
                request.workTitle(),
                request.placeName(),
                request.region(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.description(),
                request.animeImageUrl(),
                request.realImageUrl()
        );
        return toResponse(place);
    }

    @Transactional
    public void delete(Long id) {
        Place place = findPlaceOrThrow(id);
        placeRepository.delete(place);
    }

    private Place findPlaceOrThrow(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
    }

    private PlaceResponse toResponse(Place place) {
        ReviewRepository.RatingSummary summary = reviewRepository.summarizeByPlaceId(place.getId());
        return PlaceResponse.from(place, summary.getAverageRating(), summary.getReviewCount());
    }
}
