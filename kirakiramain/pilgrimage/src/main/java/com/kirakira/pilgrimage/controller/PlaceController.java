package com.kirakira.pilgrimage.controller;

import com.kirakira.pilgrimage.domain.MediaType;
import com.kirakira.pilgrimage.dto.PlaceRequest;
import com.kirakira.pilgrimage.dto.PlaceResponse;
import com.kirakira.pilgrimage.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 장소(성지) 조회는 전체 사용자, 등록/수정/삭제는 관리자 화면에서만 호출한다는 전제.
 * 인증/인가(Spring Security)는 다음 단계에서 이 컨트롤러 위에 얹는다.
 */
@RestController
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/api/places")
    public List<PlaceResponse> search(
            @RequestParam(required = false) MediaType mediaType,
            @RequestParam(required = false) String keyword
    ) {
        return placeService.search(mediaType, keyword);
    }

    @GetMapping("/api/places/{id}")
    public PlaceResponse getOne(@PathVariable Long id) {
        return placeService.getOne(id);
    }

    @PostMapping("/api/admin/places")
    public ResponseEntity<PlaceResponse> create(@Valid @RequestBody PlaceRequest request) {
        PlaceResponse response = placeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/admin/places/{id}")
    public PlaceResponse update(@PathVariable Long id, @Valid @RequestBody PlaceRequest request) {
        return placeService.update(id, request);
    }

    @DeleteMapping("/api/admin/places/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        placeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
