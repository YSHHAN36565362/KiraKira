package com.kirakira.pilgrimage.repository;

import com.kirakira.pilgrimage.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPlaceIdOrderByIdDesc(Long placeId);

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<Review> findByUserIdAndPlaceId(Long userId, Long placeId);

    @Query("""
            select coalesce(avg(r.rating), 0) as averageRating, count(r) as reviewCount
            from Review r
            where r.place.id = :placeId
            """)
    RatingSummary summarizeByPlaceId(@Param("placeId") Long placeId);

    interface RatingSummary {
        Double getAverageRating();
        Long getReviewCount();
    }
}
