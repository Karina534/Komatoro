package org.example.komatoro.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.example.komatoro.security.jwt.JwtToken;
import org.example.komatoro.security.jwt.factory.DefaultAccessTokenFactory;
import org.example.komatoro.security.jwt.serializer.AccessTokenSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTestUtil {

    @Value("${jwt.access-token-key}")
    private String secret;
    private final Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    public JwtToken generateToken(String email) {
        JwtToken token = new JwtToken(
                UUID.randomUUID(),
                email,
                Collections.singletonList("ROLE_USER"),
                Instant.now(),
                Instant.now().plusSeconds(120)
        );
        return accessTokenFactory.apply(token);
    }

    public String generateAccessTokenString(String email) {
        JwtToken token = new JwtToken(
                UUID.randomUUID(),
                email,
                Collections.singletonList("ROLE_USER"),
                Instant.now(),
                Instant.now().plusSeconds(120)
        );
        return createAccessTokenSerializer().apply(token);
    }

    private Function<JwtToken, String> createAccessTokenSerializer() {
        try {
            return new AccessTokenSerializer(new MACSigner(OctetSequenceKey.parse(secret)));
        } catch (JOSEException | java.text.ParseException exception) {
            throw new IllegalStateException("Unable to create access token serializer for tests", exception);
        }
    }
}
