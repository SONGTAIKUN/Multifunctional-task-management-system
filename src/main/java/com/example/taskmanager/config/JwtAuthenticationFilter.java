package com.example.taskmanager.config;

import com.example.taskmanager.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * This method intercepts every HTTP request once per request.
     * It checks for a valid JWT token in the Authorization header and sets the security context if valid.
    */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get the current request URI (for optional use/logging)
        String path = request.getRequestURI();

        // Retrieve the Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Extract the token by removing "Bearer " prefix
            String token = authHeader.substring(7);

            // Validate the JWT token
            if (!jwtUtil.isTokenValid(token)) {
                // If token is invalid or expired, return 401 Unauthorized
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT token.");
                return;
            }

            // Extract username and role from the token
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);  

            // Convert role to Spring Security's GrantedAuthority format
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // Create an authentication object and set it into the security context
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }else {
            // Log a warning if the Authorization header is missing or not correctly formatted
            System.out.println("[JWT Filter] ⚠️ The Authorization header was not found or was malformed.");
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
