package org.example.komatoro.security.jwt.serializer;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.security.jwt.JwtToken;

import java.util.Date;
import java.util.function.Function;

@Slf4j
public class RefreshTokenSerializer implements Function<JwtToken, String> {
    private final JWEEncrypter encrypter;

    @Getter
    @Setter
    private JWEAlgorithm algorithm = JWEAlgorithm.DIR;
    private EncryptionMethod encryptionMethod = EncryptionMethod.A256GCM;

    public RefreshTokenSerializer(JWEEncrypter encrypter) {
        this.encrypter = encrypter;
    }

    public RefreshTokenSerializer(JWEEncrypter encrypter, JWEAlgorithm algorithm, EncryptionMethod encryptionMethod) {
        this.encrypter = encrypter;
        this.algorithm = algorithm;
        this.encryptionMethod = encryptionMethod;
    }

    @Override
    public String apply(JwtToken jwtToken) {
        EncryptedJWT encryptedJWT = new EncryptedJWT(
                new JWEHeader.Builder(algorithm, encryptionMethod)
                        .keyID(jwtToken.id().toString())
                        .build(),
                new JWTClaimsSet.Builder()
                        .jwtID(jwtToken.id().toString())
                        .subject(jwtToken.subject())
                        .issueTime(Date.from(jwtToken.createdAt()))
                        .expirationTime(Date.from(jwtToken.expiresAt()))
                        .claim("authorities", jwtToken.authorities())
                        .build()
        );

        try {
            encryptedJWT.encrypt(encrypter);
            return encryptedJWT.serialize();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
        return null;
    }
}
