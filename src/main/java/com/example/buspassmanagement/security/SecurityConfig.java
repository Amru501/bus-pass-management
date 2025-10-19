package com.example.buspassmanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Added for explicit GET/POST control
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
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
                // 🔓 PUBLIC ROUTES: Allow login, register, and static resources
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll() 
                
                // 🔐 LAYER 1: ADMIN CRUD ACCESS (POST/DELETE/EDIT on Buses, Payments)
                .requestMatchers("/buses", "/buses/**", "/payments/add", "/payments/update", "/payments/delete/**").hasAuthority("ROLE_ADMIN")
                
                // 🔐 LAYER 2: DRIVER/ADMIN POSTING ACCESS (Notices)
                .requestMatchers("/notices/add", "/notices/delete/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DRIVER")
                
                // 🔐 LAYER 3: AUTHENTICATED ACCESS FOR VIEWING (List pages)
                // Payments list is visible to all authenticated users (logic handles viewing only their own)
                .requestMatchers(HttpMethod.GET, "/payments").authenticated() 
                
                // 🔑 FIX: Allow ROLE_USER (Students) AND ROLE_ADMIN to view the notices feed.
                .requestMatchers(HttpMethod.GET, "/notices").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                
                // 🔒 DEFAULT: Everything else requires authentication
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
                .key("uniqueAndSecretKey")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
            )
            .csrf(csrf -> csrf.disable()); 

        return http.build();
    }
}
