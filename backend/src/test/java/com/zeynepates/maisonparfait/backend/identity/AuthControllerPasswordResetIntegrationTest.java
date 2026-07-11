package com.zeynepates.maisonparfait.backend.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynepates.maisonparfait.backend.identity.dto.ForgotPasswordRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.LoginRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.ResetPasswordRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against real Postgres via Testcontainers - requires Docker.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerPasswordResetIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    EmailSender emailSender;

    @Test
    void forgotPasswordResetPasswordThenLoginWithNewPasswordAndOldSessionIsDead() throws Exception {
        String email = "reset-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, "password123", "Reset Test"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123", false))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie sessionBeforeReset = loginResult.getResponse().getCookie("refresh_token");

        String[] capturedBody = new String[1];
        doAnswer(invocation -> {
            capturedBody[0] = invocation.getArgument(2);
            return null;
        }).when(emailSender).send(eq(email), anyString(), anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email))))
                .andExpect(status().isAccepted());

        assertThat(capturedBody[0]).isNotNull();
        String rawToken = extractToken(capturedBody[0]);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(rawToken, "brandNewPassword456"))))
                .andExpect(status().isNoContent());

        // old password no longer works
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123", false))))
                .andExpect(status().isUnauthorized());

        // new password works
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "brandNewPassword456", false))))
                .andExpect(status().isOk());

        // the session that existed before the reset is revoked, even though
        // the reset itself didn't happen "from" that session
        mockMvc.perform(post("/api/auth/refresh").cookie(sessionBeforeReset))
                .andExpect(status().isUnauthorized());

        // the reset token itself is single-use
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(rawToken, "someOtherPassword789"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPasswordAlwaysReturnsAcceptedForUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ForgotPasswordRequest("nobody-" + System.nanoTime() + "@example.com"))))
                .andExpect(status().isAccepted());
    }

    private String extractToken(String emailBody) {
        Matcher matcher = Pattern.compile("token=([\\w-]+)").matcher(emailBody);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
