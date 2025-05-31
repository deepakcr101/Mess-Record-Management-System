package com.messmanagement.auth.filter;

import java.io.IOException;

import org.springframework.lang.NonNull; // Your UserDetailsService implementation
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.messmanagement.auth.util.JwtUtil;
import com.messmanagement.user.service.UserServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserServiceImpl userDetailsService; // Injecting your UserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // If no token, pass to the next filter
            return;
        }

        jwt = authHeader.substring(7); // "Bearer ".length()
        try {
            userEmail = jwtUtil.extractUsername(jwt); // Extract email from token

            // Check if email is present and if the user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // If token is valid, create an authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed here as JWT is already validated
                            userDetails.getAuthorities()
                    );
                    // Set details for the authentication token
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set the authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token validation failed (e.g. expired, malformed, signature error)
            // We can log this error. For now, we'll just let the request proceed without authentication.
            // The SecurityContext will remain unauthenticated, and access to protected resources will be denied.
            // Consider setting an error attribute on the request or directly handling the response for specific JWT errors if needed.
            logger.warn("JWT Token validation error: " + e.getMessage());
        }

        filterChain.doFilter(request, response); // Continue with the filter chain
    }
}