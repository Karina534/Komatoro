package org.example.komatoro.security.jwt.serializer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.security.jwt.JwtToken;

import java.util.Date;
import java.util.function.Function;

@Slf4j
public class AccessTokenSerializer implements Function<JwtToken, String> {
    private final JWSSigner jwsSigner;

    @Getter
    @Setter
    private JWSAlgorithm jwsAlgorithm = JWSAlgorithm.HS256;

    public AccessTokenSerializer(JWSSigner jwsSigner, JWSAlgorithm jwsAlgorithm) {
        this.jwsSigner = jwsSigner;
        this.jwsAlgorithm = jwsAlgorithm;
    }

    public AccessTokenSerializer(JWSSigner jwsSigner) {
        this.jwsSigner = jwsSigner;
    }

    @Override
    public String apply(JwtToken jwtToken) {
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(jwsAlgorithm).keyID(jwtToken.id().toString()).build(),
                new JWTClaimsSet.Builder()
                        .jwtID(jwtToken.id().toString())
                        .subject(jwtToken.subject())
                        .issueTime(Date.from(jwtToken.createdAt()))
                        .expirationTime(Date.from(jwtToken.expiresAt()))
                        .claim("authorities", jwtToken.authorities())
                        .build()
        );

        try {
            signedJWT.sign(this.jwsSigner);
            return signedJWT.serialize();
        } catch (JOSEException exception){
            log.error(exception.getMessage(), exception);
        }

        return null;
    }
}
