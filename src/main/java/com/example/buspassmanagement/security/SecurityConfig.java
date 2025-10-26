package com.example.buspassmanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                    "/forgot-password",
                    "/forgot-password/**",
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/animations/**"
                ).permitAll()
                
                .requestMatchers(
                    "/buses/add", 
                    "/buses/update", 
                    "/buses/edit/**", 
                    "/buses/delete/**",
                    "/payments/delete/**",
                    "/drivers/add",
                    "/notices/add",
                    "/notices/delete/**",
                    "/faq/manage",
                    "/faq/add",
                    "/faq/edit/**",
                    "/faq/delete/**",
                    "/route-installments",
                    "/route-installments/**"
                ).hasAuthority("ROLE_ADMIN")
                
                .requestMatchers(
                    "/buses", 
                    "/drivers",
                    "/faq", 
                    "/track",
                    "/payments", 
                    "/payments/installments",
                    "/payments/select-route",
                    "/payments/pay-installment",
                    "/payments/pay-all-installments",
                    "/notices", 
                    "/pass", 
                    "/profile/**"
                ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

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