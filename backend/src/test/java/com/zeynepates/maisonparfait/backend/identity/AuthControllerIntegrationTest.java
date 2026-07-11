package com.zeynepates.maisonparfait.backend.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.ResendVerificationRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.VerifyEmailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against a real Postgres via Testcontainers - requires Docker
 * to run. Replaces LoggingEmailSender with a mock so the raw verification
 * token (only ever "sent" via email, never returned by the API) can be
 * captured and fed into the verify-email call.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

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
    void registerThenVerifyEmailEndToEnd() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "integration-" + System.nanoTime() + "@example.com", "password123", "Integration Test");

        String[] capturedBody = new String[1];
        doAnswer(invocation -> {
            capturedBody[0] = invocation.getArgument(2);
            return null;
        }).when(emailSender).send(eq(registerRequest.email()), anyString(), anyString());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(registerRequest.email()))
                .andExpect(jsonPath("$.fullName").value("Integration Test"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        assertThat(capturedBody[0]).isNotNull();
        String rawToken = extractToken(capturedBody[0]);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailRequest(rawToken))))
                .andExpect(status().isNoContent());

        // single-use: the same token can't be replayed
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailRequest(rawToken))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "dup-" + System.nanoTime() + "@example.com", "password123", "Dup Test");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        RegisterRequest invalid = new RegisterRequest("not-an-email", "short", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendVerificationAlwaysReturnsAccepted() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResendVerificationRequest("nobody-" + System.nanoTime() + "@example.com"))))
                .andExpect(status().isAccepted());
    }

    private String extractToken(String emailBody) {
        Matcher matcher = Pattern.compile("token=([\\w-]+)").matcher(emailBody);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
