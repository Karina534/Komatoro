package org.example.komatoro.security.jwt.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.example.komatoro.security.jwt.JwtToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.function.Function;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JwtAuthenticationConverter implements AuthenticationConverter{
    private final Function<String, JwtToken> accessTokenStringDeserializer;
    private final Function<String, JwtToken> refreshTokenStringDeserializer;

    public JwtAuthenticationConverter(Function<String, JwtToken> accessTokenStringDeserializer, Function<String, JwtToken> refreshTokenStringDeserializer) {
        this.accessTokenStringDeserializer = accessTokenStringDeserializer;
        this.refreshTokenStringDeserializer = refreshTokenStringDeserializer;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        var authirization = request.getHeader(AUTHORIZATION);
        if (authirization != null && authirization.startsWith("Bearer ")){
            var token = authirization.substring(7);
            var accessToken = this.accessTokenStringDeserializer.apply(token);
            if (accessToken != null){
                return new PreAuthenticatedAuthenticationToken(accessToken, token);
            }

            var refreshToken = this.refreshTokenStringDeserializer.apply(token);
            if (refreshToken != null){
                return new PreAuthenticatedAuthenticationToken(refreshToken, token);
            }
        }
        return null;
    }
}
