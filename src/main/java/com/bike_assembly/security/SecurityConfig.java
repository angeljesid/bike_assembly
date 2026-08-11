package com.bike_assembly.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Permitimos que todos vean la página de inicio (/) y el /login
                .requestMatchers("/", "/login").permitAll()
                // Cualquier otra página (como /catalogo) requerirá inicio de sesión
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                // ¡Aquí está la magia! Si el login es exitoso, te lleva a /catalogo
                .defaultSuccessUrl("/catalogo", true)
                .permitAll()
            )
            .logout(logout -> logout.permitAll());
            
        return http.build();
    }
}