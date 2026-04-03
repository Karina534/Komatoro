package org.example.komatoro.security.jwt;

public record TokensResponse(
        String accessToken, String accessTokenExpiry,
        String refreshToken, String refreshTokenExpiry
) {
}
