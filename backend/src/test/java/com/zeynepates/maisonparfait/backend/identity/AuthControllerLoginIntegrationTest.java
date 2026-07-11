package com.zeynepates.maisonparfait.backend.identity;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against real Postgres via Testcontainers - requires Docker.
 * Covers login, rotation, reuse detection, and logout; account-state
 * checks (disabled/locked) are covered at the unit level in
 * AuthServiceTest since there's no way to reach those states through the
 * API yet (no admin lock endpoint until a later milestone).
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerLoginIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void loginRefreshRotationReuseDetectionAndLogout() throws Exception {
        String email = "login-" + System.nanoTime() + "@example.com";
        registerUser(email, "password123", "Login Test");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn();

        Cookie firstRefreshCookie = loginResult.getResponse().getCookie("refresh_token");
        assertThat(firstRefreshCookie).isNotNull();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh").cookie(firstRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Cookie secondRefreshCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertThat(secondRefreshCookie).isNotNull();
        assertThat(secondRefreshCookie.getValue()).isNotEqualTo(firstRefreshCookie.getValue());

        // Replaying the first (already-rotated) cookie is treated as theft:
        // the whole chain, including the second token, gets revoked.
        mockMvc.perform(post("/api/auth/refresh").cookie(firstRefreshCookie))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh").cookie(secondRefreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginThenLogoutRevokesTheSession() throws Exception {
        String email = "logout-" + System.nanoTime() + "@example.com";
        registerUser(email, "password123", "Logout Test");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123", false))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(post("/api/auth/logout").cookie(refreshCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        String email = "wrongpw-" + System.nanoTime() + "@example.com";
        registerUser(email, "password123", "Wrong Password Test");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "not-the-password", false))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("nobody-" + System.nanoTime() + "@example.com", "password123", false))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutCookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    private void registerUser(String email, String password, String fullName) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, fullName))))
                .andExpect(status().isCreated());
    }
}
