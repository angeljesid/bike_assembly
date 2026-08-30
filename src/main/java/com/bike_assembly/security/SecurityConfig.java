package com.bike_assembly.security;

import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.usuario.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public UsuarioActivoFilter usuarioActivoFilter(UsuarioDao usuarioDao) {
        return new UsuarioActivoFilter(usuarioDao);
    }

    @Bean
    public OtpVerificationFilter otpVerificationFilter(OtpService otpService, UsuarioDao usuarioDao) {
        return new OtpVerificationFilter(otpService, usuarioDao);
    }

    @Bean
    public NoCacheFilter noCacheFilter() {
        return new NoCacheFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/bloqueado/**", "/recuperar-password/**", "/css/**", "/js/**", "/images/**", "/recursos/**", "/registro").permitAll()
                .requestMatchers("/superadmin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/caja/**").hasAnyRole("ADMIN", "CAJA")
                .requestMatchers("/operario/**").hasRole("OPERARIO")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .addFilterBefore(noCacheFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(usuarioActivoFilter(usuarioDao()), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(otpVerificationFilter(otpService(), usuarioDao()), UsuarioActivoFilter.class);

        return http.build();
    }
}