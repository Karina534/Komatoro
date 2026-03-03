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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Objects;
import java.util.function.Function;

public class RefreshJwtTokenFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = new AntPathRequestMatcher("/api/users/refresh/token", "POST");

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
                if (context != null && !(context.getAuthentication() instanceof AnonymousAuthenticationToken) &&
                context.getAuthentication().getPrincipal() instanceof TokenUser tokenUser &&
                context.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_REFRESH"))){
                    var authentication = context.getAuthentication();
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
