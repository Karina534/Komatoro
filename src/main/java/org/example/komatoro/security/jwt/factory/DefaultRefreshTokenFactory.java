package org.example.komatoro.security.jwt.factory;

import lombok.Getter;
import lombok.Setter;
import org.example.komatoro.security.jwt.JwtToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Getter
@Setter
public class DefaultRefreshTokenFactory implements Function<Authentication, JwtToken> {

    private Duration tokenDuration = Duration.ofDays(3);
    @Override
    public JwtToken apply(Authentication authentication) {
        List<String> authorities = new LinkedList<>();
        authorities.add("ROLE_REFRESH");
        authorities.add("ROLE_LOGOUT");
        authorities.addAll(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> "CANCEL_" + auth)
                .toList());

        return new JwtToken(
                UUID.randomUUID(),
                authentication.getName(),
                authorities,
                Instant.now(),
                Instant.now().plus(tokenDuration)
        );
    }
}
