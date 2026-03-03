package org.example.komatoro.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.example.komatoro.security.jwt.JwtToken;
import org.example.komatoro.security.jwt.TokensResponse;
import org.example.komatoro.security.jwt.factory.DefaultAccessTokenFactory;
import org.example.komatoro.security.jwt.factory.DefaultRefreshTokenFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class RequestJwtTokenFilter extends OncePerRequestFilter {
    @Getter
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/api/users/login/token");

    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    @Getter
    @Setter
    private Function<Authentication, JwtToken> refreshTokenFactory = new DefaultRefreshTokenFactory();

    @Getter
    @Setter
    private Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    @Getter
    @Setter
    private Function<JwtToken, String> accessTokenSerializer = Objects::toString;

    @Getter
    @Setter
    private Function<JwtToken, String> refreshTokenSerializer = Objects::toString;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("------------------------------------");
        if (this.requestMatcher.matches(request)){
            System.out.println("Request matches for jwt: " + request);
            if (this.securityContextRepository.containsContext(request)){
                var context = securityContextRepository.loadDeferredContext(request).get();
                System.out.println("Authentication: " + context.getAuthentication());
                if (context != null && !(context.getAuthentication() instanceof AnonymousAuthenticationToken)){

                    var authentication = context.getAuthentication();
                    System.out.println("Start making tokens");
                    var refreshToken = this.refreshTokenFactory.apply(authentication);
                    var accessToken = this.accessTokenFactory.apply(refreshToken);

                    var tokensResponse = new TokensResponse(
                            this.accessTokenSerializer.apply(accessToken),
                            accessToken.expiresAt().toString(),
                            this.refreshTokenSerializer.apply(refreshToken),
                            refreshToken.expiresAt().toString()
                    );

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    this.objectMapper.writeValue(response.getWriter(), tokensResponse);
                    return;
                }
            }

            throw new AccessDeniedException("User must be authenticated");
        }

        filterChain.doFilter(request, response);
    }
}
