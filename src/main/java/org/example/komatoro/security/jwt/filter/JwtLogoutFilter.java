package org.example.komatoro.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.example.komatoro.security.jwt.TokenUser;
import org.example.komatoro.security.jwt.TokensResponse;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

public class JwtLogoutFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = new AntPathRequestMatcher("/api/users/logout", "POST");
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
                        context.getAuthentication().getPrincipal() instanceof TokenUser tokenUser &&
                        context.getAuthentication().getAuthorities().contains(
                                new SimpleGrantedAuthority("ROLE_LOGOUT"))){

                    this.jdbcTemplate.update("insert into deactivated_tokens(id, keep_until) values(?, ?)",
                            tokenUser.getJwtToken().id(), Date.from(tokenUser.getJwtToken().expiresAt()));

                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                }
            }

            throw new AccessDeniedException("User must be authenticated with Jwt");
        }

        filterChain.doFilter(request, response);
    }


}
