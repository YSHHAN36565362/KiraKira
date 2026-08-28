package com.kirakira.pilgrimage.repository;

import com.kirakira.pilgrimage.domain.MediaType;
import com.kirakira.pilgrimage.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("""
            select p from Place p
            where (:mediaType is null or p.mediaType = :mediaType)
              and (
                    :keyword is null
                    or lower(p.workTitle) like lower(concat('%', :keyword, '%'))
                    or lower(p.placeName) like lower(concat('%', :keyword, '%'))
                    or lower(p.region) like lower(concat('%', :keyword, '%'))
                  )
            order by p.id desc
            """)
    List<Place> search(@Param("mediaType") MediaType mediaType, @Param("keyword") String keyword);
}
