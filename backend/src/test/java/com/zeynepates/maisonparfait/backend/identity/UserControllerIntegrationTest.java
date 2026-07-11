package com.zeynepates.maisonparfait.backend.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeynepates.maisonparfait.backend.identity.dto.ChangeEmailRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.ChangePasswordRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.LoginRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.VerifyEmailRequest;
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
class UserControllerIntegrationTest {

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
    void changePasswordKeepsCallingSessionAliveButRevokesOthers() throws Exception {
        String email = "changepw-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, "password123", "Change PW Test"))))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, "password123", false);

        MvcResult sessionA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String accessTokenA = accessToken(sessionA);
        Cookie cookieA = sessionA.getResponse().getCookie("refresh_token");

        MvcResult sessionB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookieB = sessionB.getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/users/me/change-password")
                        .header("Authorization", "Bearer " + accessTokenA)
                        .cookie(cookieA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("password123", "newPassword456"))))
                .andExpect(status().isNoContent());

        // session A (the one the change was made from) survives
        mockMvc.perform(post("/api/auth/refresh").cookie(cookieA))
                .andExpect(status().isOk());

        // session B does not
        mockMvc.perform(post("/api/auth/refresh").cookie(cookieB))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123", false))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "newPassword456", false))))
                .andExpect(status().isOk());
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() throws Exception {
        String email = "changepw-wrong-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, "password123", "Wrong Current Test"))))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123", false))))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = accessToken(login);
        Cookie cookie = login.getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/users/me/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("not-the-password", "newPassword456"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeEmailRequiresConfirmationBeforeItTakesEffect() throws Exception {
        String oldEmail = "changeemail-old-" + System.nanoTime() + "@example.com";
        String newEmail = "changeemail-new-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(oldEmail, "password123", "Change Email Test"))))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(oldEmail, "password123", false))))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = accessToken(login);

        String[] confirmationBody = new String[1];
        doAnswer(invocation -> {
            confirmationBody[0] = invocation.getArgument(2);
            return null;
        }).when(emailSender).send(eq(newEmail), anyString(), anyString());

        mockMvc.perform(post("/api/users/me/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeEmailRequest(newEmail))))
                .andExpect(status().isAccepted());

        // old email still logs in - the change isn't active until confirmed
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(oldEmail, "password123", false))))
                .andExpect(status().isOk());

        assertThat(confirmationBody[0]).isNotNull();
        String rawToken = extractToken(confirmationBody[0]);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailRequest(rawToken))))
                .andExpect(status().isNoContent());

        // now the new email logs in, the old one no longer resolves to an account
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(newEmail, "password123", false))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(oldEmail, "password123", false))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeEmailRejectsAddressAlreadyTaken() throws Exception {
        String takenEmail = "taken-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(takenEmail, "password123", "Taken Email"))))
                .andExpect(status().isCreated());

        String requesterEmail = "requester-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(requesterEmail, "password123", "Requester"))))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(requesterEmail, "password123", false))))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = accessToken(login);

        mockMvc.perform(post("/api/users/me/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeEmailRequest(takenEmail))))
                .andExpect(status().isConflict());
    }

    private String accessToken(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private String extractToken(String emailBody) {
        Matcher matcher = Pattern.compile("token=([\\w-]+)").matcher(emailBody);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
