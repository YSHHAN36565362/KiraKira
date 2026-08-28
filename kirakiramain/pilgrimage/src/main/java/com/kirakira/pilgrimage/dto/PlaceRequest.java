package com.kirakira.pilgrimage.dto;

import com.kirakira.pilgrimage.domain.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceRequest(
        @NotNull MediaType mediaType,
        @NotBlank String workTitle,
        @NotBlank String placeName,
        String region,
        String address,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String description,
        String animeImageUrl,
        String realImageUrl
) {
}
