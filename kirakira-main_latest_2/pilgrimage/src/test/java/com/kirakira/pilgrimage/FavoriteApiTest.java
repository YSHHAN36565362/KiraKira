package com.kirakira.pilgrimage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirakira.pilgrimage.dto.LoginRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoriteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession loginAsNewUser(String email) throws Exception {
        SignupRequest signup = new SignupRequest(email, "password123", "찜테스터");
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
    void addFavoriteWithoutLoginIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/favorites/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addListAndRemoveFavorite() throws Exception {
        MockHttpSession session = loginAsNewUser("fav1@example.com");

        mockMvc.perform(post("/api/favorites/1").session(session))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].place.id").value(1));

        mockMvc.perform(delete("/api/favorites/1").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addingSamePlaceTwiceIsConflict() throws Exception {
        MockHttpSession session = loginAsNewUser("fav2@example.com");

        mockMvc.perform(post("/api/favorites/1").session(session))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/favorites/1").session(session))
                .andExpect(status().isConflict());
    }
}
