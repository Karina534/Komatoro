package org.example.komatoro.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.example.komatoro.security.jwt.JwtToken;
import org.example.komatoro.security.jwt.TokenUser;
import org.example.komatoro.security.jwt.TokensResponse;
import org.example.komatoro.security.jwt.factory.DefaultAccessTokenFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class RefreshJwtTokenFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/auth/refresh/token");

    @Setter
    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    @Setter
    private Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    @Setter
    private Function<JwtToken, String> accessTokenSerializer = Objects::toString;

    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (requestMatcher.matches(request)){
            if(securityContextRepository.containsContext(request)){
                var context = securityContextRepository.loadDeferredContext(request).get();
                System.out.println("Context auth: " + context.getAuthentication());
                if (context != null && !(context.getAuthentication() instanceof AnonymousAuthenticationToken) &&
                context.getAuthentication().getPrincipal() instanceof TokenUser tokenUser){

                    if (!context.getAuthentication().getAuthorities().contains(
                            new SimpleGrantedAuthority("ROLE_REFRESH"))){
                        throw new AccessDeniedException("Jwt TokenUser should be refresh token and has ROLE_REFRESH");
                    }

                    var accessToken = this.accessTokenFactory.apply(tokenUser.getJwtToken());

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(),
                            new TokensResponse((this.accessTokenSerializer.apply(accessToken)),
                            accessToken.expiresAt().toString(),
                            null, null));
                    return;
                }
            }

            throw new AccessDeniedException("User must be authenticated with Jwt");
        }

        filterChain.doFilter(request, response);
    }
}
