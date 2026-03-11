package org.example.komatoro.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.example.komatoro.security.jwt.JwtToken;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class JwtLogoutFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/auth/logout");

    @Setter
    private final Function<String, JwtToken> refreshTokenDeserializer;
    private final JdbcTemplate jdbcTemplate;

    public JwtLogoutFilter(Function<String, JwtToken> refreshTokenDeserializer, JdbcTemplate jdbcTemplate) {
        this.refreshTokenDeserializer = refreshTokenDeserializer;
        this.jdbcTemplate = jdbcTemplate;
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

                this.jdbcTemplate.update("insert into deactivated_tokens(id, keep_until) values(?, ?)",
                        refreshToken.id(), Date.from(refreshToken.expiresAt()));

                Cookie cookie1 = new Cookie("refresh-token", null);
                cookie.setHttpOnly(true);
                cookie.setPath("/auth");
                cookie.setSecure(false);
                cookie.setMaxAge(0);
                response.addCookie(cookie1);

                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            throw new AccessDeniedException("User must be authenticated with Jwt TokenUser");
        }

        filterChain.doFilter(request, response);
    }


}
