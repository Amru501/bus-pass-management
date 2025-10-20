package com.example.buspassmanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/register", 
                    "/login", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**", // For error page image
                    "/animations/**" // For error page animation
                ).permitAll()
                
                // ADMIN-ONLY MANAGEMENT ACTIONS
                .requestMatchers(
                    "/buses/add", 
                    "/buses/update", 
                    "/buses/edit/**", 
                    "/buses/delete/**",
                    "/payments/add", 
                    "/payments/create", 
                    "/payments/delete/**",
                    "/drivers/add", // Adding a driver is an admin action
                    "/notices/add", // Posting notices is now admin-only
                    "/notices/delete/**"
                ).hasAuthority("ROLE_ADMIN")
                
                // GENERAL AUTHENTICATED VIEWING
                .requestMatchers(
                    HttpMethod.GET, 
                    "/buses", 
                    "/drivers", // All authenticated users can view drivers
                    "/faq", 
                    "/track"
                ).authenticated()
                .requestMatchers("/payments", "/notices").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .rememberMe(r -> r
                .key("a-very-secret-and-unique-key-for-remember-me")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}

