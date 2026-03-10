package org.example.komatoro.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.example.komatoro.security.jwt.TokenUser;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

public class JwtLogoutFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/auth/logout");

    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
    private final JdbcTemplate jdbcTemplate;

    public JwtLogoutFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (requestMatcher.matches(request)){
            if(securityContextRepository.containsContext(request)){
                var context = securityContextRepository.loadDeferredContext(request).get();
                System.out.println("-------------");
                System.out.println("Context aith: " + context.getAuthentication());
                if (context != null && !(context.getAuthentication() instanceof AnonymousAuthenticationToken) &&
                        context.getAuthentication().getPrincipal() instanceof TokenUser tokenUser){

                    if (!context.getAuthentication().getAuthorities().contains(
                            new SimpleGrantedAuthority("ROLE_LOGOUT"))){
                        throw new AccessDeniedException("Jwt TokenUser should be refresh token and has ROLE_LOGOUT");
                    }

                    this.jdbcTemplate.update("insert into deactivated_tokens(id, keep_until) values(?, ?)",
                            tokenUser.getJwtToken().id(), Date.from(tokenUser.getJwtToken().expiresAt()));

                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                }
            }

            throw new AccessDeniedException("User must be authenticated with Jwt TokenUser");
        }

        filterChain.doFilter(request, response);
    }


}
