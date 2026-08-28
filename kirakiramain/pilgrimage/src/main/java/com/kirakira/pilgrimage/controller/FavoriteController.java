package com.kirakira.pilgrimage.controller;

import com.kirakira.pilgrimage.dto.FavoriteResponse;
import com.kirakira.pilgrimage.security.SecurityUser;
import com.kirakira.pilgrimage.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<FavoriteResponse> list(@AuthenticationPrincipal SecurityUser principal) {
        return favoriteService.list(principal.getUser().getId());
    }

    @PostMapping("/{placeId}")
    public ResponseEntity<Void> add(@PathVariable Long placeId, @AuthenticationPrincipal SecurityUser principal) {
        favoriteService.add(principal.getUser().getId(), placeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> remove(@PathVariable Long placeId, @AuthenticationPrincipal SecurityUser principal) {
        favoriteService.remove(principal.getUser().getId(), placeId);
        return ResponseEntity.noContent().build();
    }
}
