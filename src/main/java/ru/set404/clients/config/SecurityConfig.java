package ru.set404.clients.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.set404.clients.services.TherapistDetailsService;

@Configuration
@RequiredArgsConstructor
public class  SecurityConfig {

    private final JwtFilter jwtFilter;
    private final TherapistDetailsService therapistDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors()
                .and()
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers("/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html").permitAll()
                                .requestMatchers("/auth/login", "/auth/token").permitAll()
                                .requestMatchers("/therapists/create").permitAll()
                                .requestMatchers("/therapists/**").authenticated()
                                .anyRequest().permitAll()
                                .and()
                                .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                )
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(therapistDetailsService)
                .passwordEncoder(encoder())
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
