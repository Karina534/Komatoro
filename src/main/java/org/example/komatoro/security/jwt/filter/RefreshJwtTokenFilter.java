package org.example.komatoro.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.example.komatoro.security.jwt.JwtToken;
import org.example.komatoro.security.jwt.TokensResponse;
import org.example.komatoro.security.jwt.factory.DefaultAccessTokenFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class RefreshJwtTokenFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/auth/refresh/token");

    @Setter
    private Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    @Setter
    private Function<JwtToken, String> accessTokenSerializer = Objects::toString;

    @Setter
    private final Function<String, JwtToken> refreshTokenDeserializer;

    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();

    public RefreshJwtTokenFilter(Function<String, JwtToken> refreshTokenDeserializer) {
        this.refreshTokenDeserializer = refreshTokenDeserializer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (requestMatcher.matches(request)){
                if (request.getCookies() != null){
                    Optional<Cookie> cookieStream = Stream.of(request.getCookies())
                            .filter(cookie -> cookie.getName().equals("refresh-token"))
                            .findFirst();

                    if (cookieStream.isEmpty()){
                        throw new AccessDeniedException("User must have refresh token in cookie");
                    }

                    Cookie cookie = cookieStream.get();
                    var refreshToken = this.refreshTokenDeserializer.apply(cookie.getValue());
                    var accessToken = this.accessTokenFactory.apply(refreshToken);

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(),
                            new TokensResponse((this.accessTokenSerializer.apply(accessToken)),
                                    accessToken.expiresAt().toString(),
                                    null, null));
                    return;
                }

                throw new AccessDeniedException("User must be authenticated with Jwt");
            }
        filterChain.doFilter(request, response);
    }
}
