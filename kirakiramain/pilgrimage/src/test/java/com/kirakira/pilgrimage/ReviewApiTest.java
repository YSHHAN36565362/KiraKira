package com.kirakira.pilgrimage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirakira.pilgrimage.dto.LoginRequest;
import com.kirakira.pilgrimage.dto.ReviewRequest;
import com.kirakira.pilgrimage.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession loginAsNewUser(String email) throws Exception {
        SignupRequest signup = new SignupRequest(email, "password123", "리뷰테스터");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest(email, "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    void createListUpdateAndDeleteReview() throws Exception {
        MockHttpSession session = loginAsNewUser("review1@example.com");
        ReviewRequest create = new ReviewRequest(5, "정말 좋았어요");

        MvcResult createResult = mockMvc.perform(post("/api/places/1/reviews")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andReturn();

        Long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/places/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/places/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.reviewCount").value(1));

        ReviewRequest updateRequest = new ReviewRequest(3, "다시 생각해보니 보통이었어요");
        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));

        mockMvc.perform(delete("/api/reviews/" + reviewId).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/places/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void creatingSecondReviewForSamePlaceIsConflict() throws Exception {
        MockHttpSession session = loginAsNewUser("review2@example.com");
        ReviewRequest request = new ReviewRequest(4, "좋아요");

        mockMvc.perform(post("/api/places/1/reviews")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/places/1/reviews")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updatingSomeoneElsesReviewIsForbidden() throws Exception {
        MockHttpSession authorSession = loginAsNewUser("author@example.com");
        ReviewRequest request = new ReviewRequest(4, "좋아요");
        MvcResult createResult = mockMvc.perform(post("/api/places/1/reviews")
                        .session(authorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        Long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        MockHttpSession otherSession = loginAsNewUser("other@example.com");
        ReviewRequest updateRequest = new ReviewRequest(1, "수정 시도");
        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .session(otherSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }
}
