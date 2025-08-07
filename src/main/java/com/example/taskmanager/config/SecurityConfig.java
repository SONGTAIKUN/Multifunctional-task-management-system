package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableMethodSecurity   // Enables method-level security (e.g., @PreAuthorize)
public class SecurityConfig {

    // Inject the custom JWT authentication filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Defines a password encoder bean using BCrypt (secure hashing for passwords).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the main security filter chain configuration.
     *
     * @param http the HttpSecurity object to customize security behavior
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

            // Disable CSRF protection (mainly for APIs)
            .csrf(csrf -> csrf.disable())  

            // Disable request caching (no need to remember previous pages)
            .requestCache(cache -> cache.requestCache(new NullRequestCache()))

            // Disable default Spring login form (we use JWT instead)
            .formLogin(form -> form.disable())  

            // Define authorization rules
            .authorizeHttpRequests(auth -> auth

                // Publicly accessible endpoints (no authentication required)
                .requestMatchers(
                    "/login",           
                    "/api/register",
                    "/api/avatar/**",
                    "/static/**",       
                    "/login.html",     
                    "/register.html",
                    "/changepassword.html",
                    "/uploadavatar.html",
                    "/createtask.html",
                    "/userinfo.html",      
                    "/edittask.html",
                    "/filtertask.html",
                    "/admin.html",
                    "/", "/favicon.ico"
                ).permitAll()

                // Only users with ROLE_ADMIN can access /api/admin/**
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )

            // Add the JWT filter before the default authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // Finalize the security configuration
            .build();
    }
    
}
