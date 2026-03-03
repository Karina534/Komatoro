package org.example.komatoro.config;

import jakarta.servlet.http.HttpServletResponse;
import org.example.komatoro.security.jwt.JwtToken;
import org.example.komatoro.security.jwt.factory.DefaultAccessTokenFactory;
import org.example.komatoro.security.jwt.factory.DefaultRefreshTokenFactory;
import org.example.komatoro.security.jwt.filter.JwtAuthenticationConverter;
import org.example.komatoro.security.jwt.filter.JwtLogoutFilter;
import org.example.komatoro.security.jwt.filter.RefreshJwtTokenFilter;
import org.example.komatoro.security.jwt.filter.RequestJwtTokenFilter;
import org.example.komatoro.security.jwt.service.JwtAuthenticationUserDetailsService;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Objects;
import java.util.function.Function;

/**
 * Конфигуратор, который добавляет в цепочку фильтров фильтры отвечающие за работу с jwt токенами
 */
//TODO: Найти альтернативу AbstractHttpConfigurer чтобы от не расширял SecurityConfigurerAdapter, из-за которого в
// filterChain метод apply помечен как Depricated
public class JwtAuthenticationConfigurer
        extends AbstractHttpConfigurer<JwtAuthenticationConfigurer, HttpSecurity> {

//    private RequestMatcher requestMatcher = PathPatternRequestMatcher
//            .withDefaults().matcher(HttpMethod.POST, "/api/users/login/token");

    private RequestMatcher requestMatcher = new AntPathRequestMatcher("/api/users/login/token", HttpMethod.POST.name());
    private Function<Authentication, JwtToken> refreshTokenFactory = new DefaultRefreshTokenFactory();

    private Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    private Function<JwtToken, String> accessTokenSerializer = Objects::toString;

    private Function<JwtToken, String> refreshTokenSerializer = Objects::toString;
    private Function<String, JwtToken> accessTokenStringDeserializer;
    private Function<String, JwtToken> refreshTokenStringDeserializer;
    private JdbcTemplate jdbcTemplate;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        var csrfConfigurer = builder.getConfigurer(CsrfConfigurer.class);
        if (csrfConfigurer != null){
            csrfConfigurer.ignoringRequestMatchers(this.requestMatcher);
        }
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        var requestJwtTokenFilter = new RequestJwtTokenFilter();
        requestJwtTokenFilter.setRequestMatcher(this.requestMatcher);
        requestJwtTokenFilter.setRefreshTokenFactory(this.refreshTokenFactory);
        requestJwtTokenFilter.setAccessTokenFactory(this.accessTokenFactory);
        requestJwtTokenFilter.setRefreshTokenSerializer(this.refreshTokenSerializer);
        requestJwtTokenFilter.setAccessTokenSerializer(this.accessTokenSerializer);

        var jwtAuthenticationFilter = new AuthenticationFilter(
                builder.getSharedObject(AuthenticationManager.class),
                new JwtAuthenticationConverter(
                    this.accessTokenStringDeserializer,
                    this.refreshTokenStringDeserializer
                )
        );
        jwtAuthenticationFilter.setSuccessHandler((request, response, authentication) -> {
            CsrfFilter.skipRequest(request);
        });
        jwtAuthenticationFilter.setFailureHandler((request, response, exception) -> {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        });

        var refreshJwtTokenFilter = new RefreshJwtTokenFilter();
        refreshJwtTokenFilter.setAccessTokenFactory(this.accessTokenFactory);
        refreshJwtTokenFilter.setAccessTokenSerializer(this.accessTokenSerializer);

        var jwtLogoutFilter = new JwtLogoutFilter(this.jdbcTemplate);

        System.out.println("---------------------");
        System.out.println("Added configurer for jwt");

        var provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(new JwtAuthenticationUserDetailsService(this.jdbcTemplate));

        builder.addFilterBefore(requestJwtTokenFilter, ExceptionTranslationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, CsrfFilter.class)
                .addFilterBefore(refreshJwtTokenFilter, ExceptionTranslationFilter.class)
                .addFilterBefore(jwtLogoutFilter, ExceptionTranslationFilter.class)
                .authenticationProvider(provider);
    }

    public JwtAuthenticationConfigurer requestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
        return this;
    }

    public JwtAuthenticationConfigurer refreshTokenFactory(Function<Authentication, JwtToken> refreshTokenFactory) {
        this.refreshTokenFactory = refreshTokenFactory;
        return this;
    }

    public JwtAuthenticationConfigurer accessTokenFactory(Function<JwtToken, JwtToken> accessTokenFactory) {
        this.accessTokenFactory = accessTokenFactory;
        return this;
    }

    public JwtAuthenticationConfigurer accessTokenSerializer(Function<JwtToken, String> accessTokenSerializer) {
        this.accessTokenSerializer = accessTokenSerializer;
        return this;
    }

    public JwtAuthenticationConfigurer refreshTokenSerializer(Function<JwtToken, String> refreshTokenSerializer) {
        this.refreshTokenSerializer = refreshTokenSerializer;
        return this;
    }

    public JwtAuthenticationConfigurer accessTokenStringDeserializer(Function<String, JwtToken> accessTokenStringDeserializer) {
        this.accessTokenStringDeserializer = accessTokenStringDeserializer;
        return this;
    }

    public JwtAuthenticationConfigurer refreshTokenStringDeserializer(Function<String, JwtToken> refreshTokenStringDeserializer) {
        this.refreshTokenStringDeserializer = refreshTokenStringDeserializer;
        return this;
    }

    public JwtAuthenticationConfigurer jdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        return this;
    }
}
