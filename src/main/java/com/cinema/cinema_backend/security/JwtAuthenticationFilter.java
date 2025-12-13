package com.cinema.cinema_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Логирование для отладки
        System.out.println("\n=== JWT FILTER DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authorizationHeader);

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT Token (first 20 chars): " +
                    (jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt));

            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Extracted username from JWT: " + username);
            } catch (Exception e) {
                System.out.println("Error extracting username: " + e.getMessage());
            }
        } else {
            System.out.println("No Bearer token found or invalid format");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Attempting to load user details for: " + username);

            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("UserDetails loaded: " + userDetails.getUsername());
                System.out.println("Authorities: " + userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    System.out.println("Token is VALID");

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("Security context set with authentication");
                } else {
                    System.out.println("Token is INVALID");
                }
            } catch (Exception e) {
                System.out.println("Error loading user details: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Username is null or already authenticated");
            System.out.println("Username null? " + (username == null));
            System.out.println("Already authenticated? " +
                    (SecurityContextHolder.getContext().getAuthentication() != null));
        }

        System.out.println("=== JWT FILTER END ===\n");
        chain.doFilter(request, response);
    }
}