package org.example.komatoro.security.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class TokenUser extends User {

    @Getter
    private final JwtToken jwtToken;

    public TokenUser(String username, String password, Collection<? extends GrantedAuthority> authorities, JwtToken jwtToken) {
        super(username, password, authorities);
        this.jwtToken = jwtToken;
    }

    public TokenUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, JwtToken jwtToken) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.jwtToken = jwtToken;
    }
}
