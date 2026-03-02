package org.example.komatoro.security.jwt.serializer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.security.jwt.JwtToken;

import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class JwtAccessTokenJwsStringDeserializer implements Function<String, JwtToken> {

    private final JWSVerifier jwsVerifier;

    @Setter
    private JWSAlgorithm jwsAlgorithm = JWSAlgorithm.HS256;

    public JwtAccessTokenJwsStringDeserializer(JWSVerifier jwsVerifier) {
        this.jwsVerifier = jwsVerifier;
    }

    public JwtAccessTokenJwsStringDeserializer(JWSVerifier jwsVerifier, JWSAlgorithm jwsAlgorithm) {
        this.jwsVerifier = jwsVerifier;
        this.jwsAlgorithm = jwsAlgorithm;
    }

    @Override
    public JwtToken apply(String s) {
        try{
            var signedJwt = SignedJWT.parse(s);
            if (signedJwt.verify(this.jwsVerifier)){
                var claimSet = signedJwt.getJWTClaimsSet();
                return new JwtToken(
                        UUID.fromString(claimSet.getJWTID()),
                        claimSet.getSubject(),
                        claimSet.getStringListClaim("authorities"),
                        claimSet.getIssueTime().toInstant(),
                        claimSet.getExpirationTime().toInstant()
                );
            }

        } catch (ParseException | JOSEException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }
}
