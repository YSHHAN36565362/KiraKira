package com.kirakira.pilgrimage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType mediaType;

    @Column(nullable = false, length = 100)
    private String workTitle;

    @Column(nullable = false, length = 100)
    private String placeName;

    @Column(length = 100)
    private String region;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Lob
    private String description;

    @Column(length = 500)
    private String animeImageUrl;

    @Column(length = 500)
    private String realImageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Place(MediaType mediaType, String workTitle, String placeName, String region,
                 String address, Double latitude, Double longitude, String description,
                 String animeImageUrl, String realImageUrl) {
        this.mediaType = mediaType;
        this.workTitle = workTitle;
        this.placeName = placeName;
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.animeImageUrl = animeImageUrl;
        this.realImageUrl = realImageUrl;
    }

    public void update(MediaType mediaType, String workTitle, String placeName, String region,
                        String address, Double latitude, Double longitude, String description,
                        String animeImageUrl, String realImageUrl) {
        this.mediaType = mediaType;
        this.workTitle = workTitle;
        this.placeName = placeName;
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.animeImageUrl = animeImageUrl;
        this.realImageUrl = realImageUrl;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
