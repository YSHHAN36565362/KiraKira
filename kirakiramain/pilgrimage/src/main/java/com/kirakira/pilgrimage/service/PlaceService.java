package com.kirakira.pilgrimage.service;

import com.kirakira.pilgrimage.domain.MediaType;
import com.kirakira.pilgrimage.domain.Place;
import com.kirakira.pilgrimage.dto.PlaceRequest;
import com.kirakira.pilgrimage.dto.PlaceResponse;
import com.kirakira.pilgrimage.exception.PlaceNotFoundException;
import com.kirakira.pilgrimage.repository.PlaceRepository;
import com.kirakira.pilgrimage.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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

    public byte[] exportCsv() {
        List<Place> places = placeRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append('﻿'); // 엑셀에서 한글이 깨지지 않도록 하는 BOM
        csv.append("ID,미디어유형,작품명,장소명,지역,주소,위도,경도,평균평점,리뷰수\n");

        for (Place place : places) {
            ReviewRepository.RatingSummary summary = reviewRepository.summarizeByPlaceId(place.getId());
            csv.append(place.getId()).append(",")
                    .append(escapeCsv(place.getMediaType().name())).append(",")
                    .append(escapeCsv(place.getWorkTitle())).append(",")
                    .append(escapeCsv(place.getPlaceName())).append(",")
                    .append(escapeCsv(place.getRegion())).append(",")
                    .append(escapeCsv(place.getAddress())).append(",")
                    .append(place.getLatitude()).append(",")
                    .append(place.getLongitude()).append(",")
                    .append(summary.getAverageRating()).append(",")
                    .append(summary.getReviewCount()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportExcel() {
        List<Place> places = placeRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("장소 목록");

            String[] headers = {"ID", "미디어유형", "작품명", "장소명", "지역", "주소", "위도", "경도", "평균평점", "리뷰수"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (Place place : places) {
                ReviewRepository.RatingSummary summary = reviewRepository.summarizeByPlaceId(place.getId());
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(place.getId());
                row.createCell(1).setCellValue(place.getMediaType().name());
                row.createCell(2).setCellValue(place.getWorkTitle());
                row.createCell(3).setCellValue(place.getPlaceName());
                row.createCell(4).setCellValue(place.getRegion());
                row.createCell(5).setCellValue(place.getAddress());
                row.createCell(6).setCellValue(place.getLatitude());
                row.createCell(7).setCellValue(place.getLongitude());
                row.createCell(8).setCellValue(summary.getAverageRating());
                row.createCell(9).setCellValue(summary.getReviewCount());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
