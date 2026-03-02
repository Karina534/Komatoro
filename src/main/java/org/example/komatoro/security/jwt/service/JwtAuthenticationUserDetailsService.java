package org.example.komatoro.security.jwt.service;

import org.example.komatoro.security.jwt.JwtToken;
import org.example.komatoro.security.jwt.TokenUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.time.Instant;

public class JwtAuthenticationUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        System.out.println("----------------------------");
        System.out.println("I am in JwtAuthenticationUserDetailsService");
        if (authenticationToken.getPrincipal() instanceof JwtToken token){
            return new TokenUser(token.subject(), "nopass", true,true,
                    token.expiresAt().isAfter(Instant.now()), true,
                    token.authorities().stream().map(SimpleGrantedAuthority::new).toList(),
                    token);
        }
        return null;
    }
}
