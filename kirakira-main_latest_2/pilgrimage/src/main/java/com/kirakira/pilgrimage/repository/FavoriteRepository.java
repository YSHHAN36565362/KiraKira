package com.kirakira.pilgrimage.repository;

import com.kirakira.pilgrimage.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
    Optional<Favorite> findByUserIdAndPlaceId(Long userId, Long placeId);
    List<Favorite> findByUserIdOrderByIdDesc(Long userId);
}
