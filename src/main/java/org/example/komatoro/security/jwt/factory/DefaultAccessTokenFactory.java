package org.example.komatoro.security.jwt.factory;

import lombok.Getter;
import lombok.Setter;
import org.example.komatoro.security.jwt.JwtToken;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class DefaultAccessTokenFactory implements Function<JwtToken, JwtToken> {
    private Duration tokenDuration = Duration.ofMinutes(15);
    @Override
    public JwtToken apply(JwtToken jwtToken) {
        List<String> authorities = jwtToken.authorities().stream()
                .filter(a -> !a.equals("ROLE_REFRESH") && !a.equals("ROLE_LOGOUT")).toList();
        return new JwtToken(
                jwtToken.id(),
                jwtToken.subject(),
                authorities,
                jwtToken.createdAt(),
                Instant.now().plus(tokenDuration)
        );
    }
}
