package org.example.komatoro.security.jwt.serializer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.security.jwt.JwtToken;

import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class JwtRefreshTokenJweStringDeserializer implements Function<String, JwtToken> {
    private final JWEDecrypter jweDecrypter;

    public JwtRefreshTokenJweStringDeserializer(JWEDecrypter jweDecrypter) {
        this.jweDecrypter = jweDecrypter;
    }

    @Override
    public JwtToken apply(String s) {
        try{
            var encryptedJwt = EncryptedJWT.parse(s);
            encryptedJwt.decrypt(this.jweDecrypter);
            var claimSet = encryptedJwt.getJWTClaimsSet();
            return new JwtToken(
                    UUID.fromString(claimSet.getJWTID()),
                    claimSet.getSubject(),
                    claimSet.getStringListClaim("authorities"),
                    claimSet.getIssueTime().toInstant(),
                    claimSet.getExpirationTime().toInstant()
            );

        } catch (ParseException | JOSEException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
