package com.kirakira.pilgrimage;

import com.kirakira.pilgrimage.dto.PlaceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlaceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchReturnsSeededPlaces() throws Exception {
        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(30));
    }

    @Test
    void searchFiltersByMediaTypeAndKeyword() throws Exception {
        mockMvc.perform(get("/api/places").param("mediaType", "ANIME").param("keyword", "스가"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placeName").value("스가 신사"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPlaceThenFetchById() throws Exception {
        PlaceRequest request = new PlaceRequest(
                com.kirakira.pilgrimage.domain.MediaType.DRAMA,
                "테스트 드라마",
                "테스트 장소",
                "도쿄",
                "Tokyo, Japan",
                35.0, 139.0,
                "설명",
                null,
                null
        );

        mockMvc.perform(post("/api/admin/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placeName").value("테스트 장소"));
    }
}
