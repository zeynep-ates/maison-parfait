package com.zeynepates.maisonparfait.backend.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynepates.maisonparfait.backend.identity.dto.LoginRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against real Postgres via Testcontainers - requires Docker.
 * Simulates two devices by logging in twice with the same credentials,
 * producing two independent refresh_tokens rows (sessions).
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void listRevokeOneAndRevokeAllExceptCurrent() throws Exception {
        String email = "sessions-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, "password123", "Sessions Test"))))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, "password123", false);

        MvcResult deviceA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String accessTokenA = accessToken(deviceA);
        Cookie cookieA = deviceA.getResponse().getCookie("refresh_token");

        MvcResult deviceB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(deviceB.getResponse().getCookie("refresh_token").getValue())
                .isNotEqualTo(cookieA.getValue());

        // From device A's point of view: two active sessions, itself flagged current.
        MvcResult listResult = mockMvc.perform(get("/api/users/me/sessions")
                        .header("Authorization", "Bearer " + accessTokenA)
                        .cookie(cookieA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode sessions = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(sessions).hasSize(2);
        long currentCount = 0;
        long otherSessionId = -1;
        for (JsonNode session : sessions) {
            if (session.get("current").asBoolean()) {
                currentCount++;
            } else {
                otherSessionId = session.get("id").asLong();
            }
        }
        assertThat(currentCount).isEqualTo(1);
        assertThat(otherSessionId).isNotEqualTo(-1);

        // Revoke device B's session from device A.
        mockMvc.perform(delete("/api/users/me/sessions/" + otherSessionId)
                        .header("Authorization", "Bearer " + accessTokenA)
                        .cookie(cookieA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me/sessions")
                        .header("Authorization", "Bearer " + accessTokenA)
                        .cookie(cookieA))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode remaining = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(remaining).hasSize(1);
                });

        // Revoking device B's already-revoked session again 404s rather than
        // silently succeeding a second time.
        mockMvc.perform(delete("/api/users/me/sessions/" + otherSessionId)
                        .header("Authorization", "Bearer " + accessTokenA)
                        .cookie(cookieA))
                .andExpect(status().isNotFound());

        // Device A's own refresh token is untouched by all of this.
        mockMvc.perform(post("/api/auth/refresh").cookie(cookieA))
                .andExpect(status().isOk());
    }

    @Test
    void revokeAllExceptCurrentKeepsOnlyTheCallingSession() throws Exception {
        String email = "revoke-all-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, "password123", "Revoke All Test"))))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, "password123", false);

        MvcResult keep = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String keepAccessToken = accessToken(keep);
        Cookie keepCookie = keep.getResponse().getCookie("refresh_token");

        MvcResult other = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie otherCookie = other.getResponse().getCookie("refresh_token");

        mockMvc.perform(delete("/api/users/me/sessions")
                        .header("Authorization", "Bearer " + keepAccessToken)
                        .cookie(keepCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh").cookie(keepCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh").cookie(otherCookie))
                .andExpect(status().isUnauthorized());
    }

    private String accessToken(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
