package com.kirakira.pilgrimage.dto;

import com.kirakira.pilgrimage.domain.Favorite;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long favoriteId,
        PlaceResponse place,
        LocalDateTime createdAt
) {
    public static FavoriteResponse from(Favorite favorite, PlaceResponse placeResponse) {
        return new FavoriteResponse(favorite.getId(), placeResponse, favorite.getCreatedAt());
    }
}
