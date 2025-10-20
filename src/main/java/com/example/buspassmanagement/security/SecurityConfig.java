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
                // --- RULE ORDER: MOST SPECIFIC TO MOST GENERAL ---

                // 1. PUBLIC ASSETS & PAGES (Accessible to everyone)
                .requestMatchers(
                    "/register", 
                    "/login", 
                    "/error", // Allow access to the custom error page
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/webjars/**",
                    "/animations/**" // *** NEW: Allow access to animations folder ***
                ).permitAll()

                // 2. ADMIN-ONLY MANAGEMENT ACTIONS
                .requestMatchers("/buses/add", "/buses/update", "/buses/edit/**", "/buses/delete/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/payments/add", "/payments/create", "/payments/delete/**").hasAuthority("ROLE_ADMIN")
                
                // 3. DRIVER & ADMIN ACTIONS
                .requestMatchers("/notices/add", "/notices/delete/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DRIVER")

                // 4. GENERAL AUTHENTICATED VIEWING
                .requestMatchers("/track").authenticated() // Secure the new track page
                .requestMatchers(HttpMethod.GET, "/buses").authenticated() 
                .requestMatchers("/payments", "/notices").authenticated()

                // 5. CATCH-ALL
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

