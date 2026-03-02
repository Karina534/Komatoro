package org.example.komatoro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.security.jwt.serializer.AccessTokenSerializer;
import org.example.komatoro.security.jwt.serializer.JwtAccessTokenJwsStringDeserializer;
import org.example.komatoro.security.jwt.serializer.JwtRefreshTokenJweStringDeserializer;
import org.example.komatoro.security.jwt.serializer.RefreshTokenSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConfigurer jwtConfigurer(
            @Value("${jwt.access-token-key}") String accessTokenKey,
            @Value("${jwt.refresh-token-key}") String refreshTokenKey
    ) throws ParseException, JOSEException {
        return new JwtAuthenticationConfigurer()
                .accessTokenSerializer(new AccessTokenSerializer(
                        new MACSigner(OctetSequenceKey.parse(accessTokenKey))
                ))
                .refreshTokenSerializer(new RefreshTokenSerializer(
                        new DirectEncrypter(OctetSequenceKey.parse(refreshTokenKey))
                ))
                .accessTokenStringDeserializer(new JwtAccessTokenJwsStringDeserializer(
                        new MACVerifier(OctetSequenceKey.parse(accessTokenKey))
                ))
                .refreshTokenStringDeserializer(new JwtRefreshTokenJweStringDeserializer(
                        new DirectDecrypter(OctetSequenceKey.parse(refreshTokenKey))
                ));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConfigurer jwtAuthenticationConfigurer)
            throws Exception {

        return http
                .csrf(CsrfConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/users/login", "/api/users/registration").permitAll()
                        .requestMatchers("/login.html", "/home.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .anyRequest().hasAnyAuthority("ROEL_USER", "ROLE_ADMIN")
                )
//                .formLogin(form -> form
//                        .loginPage("/api/users/login")
//                        .loginProcessingUrl("/api/users/login")
//                        .permitAll()
//                        .usernameParameter("email")
//                        .defaultSuccessUrl("/api/users/home")
////                        .successHandler(jsonAuthenticationSuccessHandler())
//                        .failureHandler(jsonAuthenticationFailureHandler())
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/api/users/logout")
//                        .logoutSuccessUrl("/api/users/login")
//                        .permitAll()
//                )
                .apply(jwtAuthenticationConfigurer)
                .and()
                .build();
    }

    private AuthenticationFailureHandler jsonAuthenticationFailureHandler() {
        return ((request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> failureResponse = new HashMap<>();
            failureResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            failureResponse.put("error", "Authentication failed");
            failureResponse.put("message",  "Invalid email or password");

            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(failureResponse));
            writer.flush();
        });
    }

    private AuthenticationSuccessHandler jsonAuthenticationSuccessHandler() {
        return ((request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Login success");
            successResponse.put("email", authentication.getName());
            successResponse.put("roles", authentication.getAuthorities());

            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(successResponse));
            writer.flush();
        });
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
