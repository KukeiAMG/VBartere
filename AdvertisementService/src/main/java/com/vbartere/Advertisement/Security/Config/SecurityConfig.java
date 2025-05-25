package com.vbartere.Advertisement.Security.Config;


import com.vbartere.Advertisement.Security.JwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private static final String INTERNAL_SECRET = "payment-service-secret";

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/advertisements/create").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/advertisements/{id}/update").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/advertisements/{id}/delete").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/advertisements/{id}/update-fields").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/advertisements/{id}/update-images").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/images/{id}").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/advertisements/owners")
                        .access(internalHeaderAuthorizationManager())
//                        .requestMatchers("/api/subcategory/create", "/api/subcategory/{id}/delete",
//                                "/api/subcategory/{id}/update").hasAuthority("ROLE_ADMIN")
//                        .requestMatchers("/api/category/create", "/api/category/{id}/update",
//                                "/api/category/{id}/delete").hasAuthority("ROLE_ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    public AuthorizationManager<RequestAuthorizationContext> internalHeaderAuthorizationManager() {
        return (authenticationSupplier, context) -> {
            HttpServletRequest request = context.getRequest();
            String header = request.getHeader("Internal-Call");
            boolean granted = INTERNAL_SECRET.equals(header);
            return new AuthorizationDecision(granted);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

