package com.kirakira.pilgrimage.dto;

import com.kirakira.pilgrimage.domain.MediaType;
import com.kirakira.pilgrimage.domain.Place;

import java.time.LocalDateTime;

public record PlaceResponse(
        Long id,
        MediaType mediaType,
        String workTitle,
        String placeName,
        String region,
        String address,
        Double latitude,
        Double longitude,
        String description,
        String animeImageUrl,
        String realImageUrl,
        Double averageRating,
        Long reviewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlaceResponse from(Place place, Double averageRating, Long reviewCount) {
        return new PlaceResponse(
                place.getId(),
                place.getMediaType(),
                place.getWorkTitle(),
                place.getPlaceName(),
                place.getRegion(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getDescription(),
                place.getAnimeImageUrl(),
                place.getRealImageUrl(),
                averageRating,
                reviewCount,
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }
}
