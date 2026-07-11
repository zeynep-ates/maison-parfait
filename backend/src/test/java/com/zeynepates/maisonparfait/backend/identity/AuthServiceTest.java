package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.ForbiddenException;
import com.zeynepates.maisonparfait.backend.common.exception.UnauthorizedException;
import com.zeynepates.maisonparfait.backend.identity.dto.LoginRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.UserResponse;
import com.zeynepates.maisonparfait.backend.identity.mapper.UserMapper;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import com.zeynepates.maisonparfait.backend.modules.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    UserMapper userMapper;

    @Mock
    TokenService tokenService;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    AuthService authService;

    @Test
    void registerHashesPasswordSavesUserAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest("jane@example.com", "password123", "Jane Doe");

        when(userRepository.existsByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(new UserResponse(1L, "jane@example.com", "Jane Doe", "CUSTOMER", false, null));

        UserResponse response = authService.register(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getEmail()).isEqualTo("jane@example.com");
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getValue().getFullName()).isEqualTo("Jane Doe");
        assertThat(savedUser.getValue().getEnabled()).isTrue();

        verify(emailVerificationService).sendVerificationEmail(savedUser.getValue());
        assertThat(response.email()).isEqualTo("jane@example.com");
    }

    @Test
    void registerRejectsAlreadyRegisteredEmail() {
        RegisterRequest request = new RegisterRequest("jane@example.com", "password123", "Jane Doe");
        when(userRepository.existsByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailVerificationService, userMapper);
    }

    @Test
    void loginIssuesTokensForCorrectCredentials() {
        User user = activeUser();
        LoginRequest request = new LoginRequest("jane@example.com", "password123", false);

        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(tokenService.generateAccessToken(1L, UserRole.CUSTOMER)).thenReturn("access-token");
        when(tokenService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(eq(user), any(), any(), eq(false)))
                .thenReturn(new RefreshTokenService.IssuedRefreshToken("raw-refresh", OffsetDateTime.now().plusDays(7)));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse(1L, "jane@example.com", "Jane Doe", "CUSTOMER", true, null));

        IssuedTokens issued = authService.login(request, "UA", "127.0.0.1");

        assertThat(issued.response().accessToken()).isEqualTo("access-token");
        assertThat(issued.rawRefreshToken()).isEqualTo("raw-refresh");
    }

    @Test
    void loginRejectsUnknownEmailWithGenericMessage() {
        LoginRequest request = new LoginRequest("ghost@example.com", "password123", false);
        when(userRepository.findByEmailAndDeletedAtIsNull("ghost@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(eq("password123"), any())).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(request, "UA", "127.0.0.1"));

        assertThat(ex.getMessage()).isEqualTo("Invalid email or password");
        verifyNoInteractions(tokenService, refreshTokenService);
    }

    @Test
    void loginRejectsWrongPasswordWithSameGenericMessageAsUnknownEmail() {
        User user = activeUser();
        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password", false);

        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(request, "UA", "127.0.0.1"));

        assertThat(ex.getMessage()).isEqualTo("Invalid email or password");
    }

    // Soft-deleted accounts are excluded by findByEmailAndDeletedAtIsNull
    // itself (see AuthService.login's comment), so from this test's point
    // of view a deleted account and a never-registered one are the exact
    // same case, already covered by loginRejectsUnknownEmailWithGenericMessage.

    @Test
    void loginRejectsDisabledAccountAfterPasswordIsVerified() {
        User user = activeUser();
        user.setEnabled(false);
        LoginRequest request = new LoginRequest("jane@example.com", "password123", false);

        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> authService.login(request, "UA", "127.0.0.1"));
        assertThat(ex.getMessage()).isEqualTo("Account disabled");
        verifyNoInteractions(tokenService, refreshTokenService);
    }

    @Test
    void loginRejectsLockedAccountAfterPasswordIsVerified() {
        User user = activeUser();
        user.setLockedAt(OffsetDateTime.now());
        LoginRequest request = new LoginRequest("jane@example.com", "password123", false);

        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> authService.login(request, "UA", "127.0.0.1"));
    }

    @Test
    void refreshDelegatesToRefreshTokenServiceAndReturnsNewTokens() {
        User user = activeUser();
        RefreshTokenService.IssuedRefreshToken rotated =
                new RefreshTokenService.IssuedRefreshToken("raw-new", OffsetDateTime.now().plusDays(7));
        when(refreshTokenService.rotate("raw-old", "UA", "127.0.0.1"))
                .thenReturn(new RefreshTokenService.RotationResult(user, rotated));
        when(tokenService.generateAccessToken(anyLong(), any())).thenReturn("access-token-2");
        when(tokenService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(userMapper.toResponse(user)).thenReturn(new UserResponse(1L, "jane@example.com", "Jane Doe", "CUSTOMER", true, null));

        IssuedTokens issued = authService.refresh("raw-old", "UA", "127.0.0.1");

        assertThat(issued.rawRefreshToken()).isEqualTo("raw-new");
        assertThat(issued.response().accessToken()).isEqualTo("access-token-2");
    }

    @Test
    void logoutDelegatesToRefreshTokenServiceRevoke() {
        authService.logout("raw-token");

        verify(refreshTokenService).revoke("raw-token");
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setRole(UserRole.CUSTOMER);
        user.setEnabled(true);
        return user;
    }
}
