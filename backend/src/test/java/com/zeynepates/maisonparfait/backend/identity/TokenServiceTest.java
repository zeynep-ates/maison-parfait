package com.zeynepates.maisonparfait.backend.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService("test-secret-at-least-32-characters-long", 900);

    @Test
    void generatesAndParsesRoundTrip() {
        String token = tokenService.generateAccessToken(42L, UserRole.CUSTOMER);

        var principal = tokenService.parse(token);

        assertThat(principal).isPresent();
        assertThat(principal.get().userId()).isEqualTo(42L);
        assertThat(principal.get().role()).isEqualTo("CUSTOMER");
    }

    @Test
    void rejectsTamperedSignature() {
        String token = tokenService.generateAccessToken(42L, UserRole.CUSTOMER);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(tokenService.parse(tampered)).isEmpty();
    }

    @Test
    void rejectsGarbageInput() {
        assertThat(tokenService.parse("not-a-jwt")).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        TokenService alreadyExpired = new TokenService("test-secret-at-least-32-characters-long", -1);
        String token = alreadyExpired.generateAccessToken(42L, UserRole.CUSTOMER);

        assertThat(alreadyExpired.parse(token)).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        TokenService other = new TokenService("a-completely-different-secret-32-chars-plus", 900);
        String token = other.generateAccessToken(42L, UserRole.CUSTOMER);

        assertThat(tokenService.parse(token)).isEmpty();
    }
}
