package com.luxshop.shop.config;

import com.luxshop.shop.security.JwtAuthenticationFilter;
import com.luxshop.shop.security.MyUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class CustomWebSecurityConfiguration {

    @Autowired
    public MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // Enable CORS so the Next.js frontend (localhost:3000) can call the API.
                .cors(Customizer.withDefaults())
                // Stateless REST API authenticated with JWT -> CSRF protection is not applicable.
                .csrf(csrf -> csrf.disable())
                // Allow the H2 console to render inside a frame.
                .headers(h -> h.frameOptions(fo -> fo.disable()))
                .authorizeHttpRequests(auth -> auth
                        // API documentation is public.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui.html")).permitAll()
                        // Login is public; everything else authenticates with a Bearer token.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/auth/**")).permitAll()
                        // Public storefront browsing: read-only catalog access (controllers live under /shop).
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/products/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/product/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/categories/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/category/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/coupon/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/search/**")).permitAll()
                        // Guest product reviews: anyone can leave one (GET is covered by /shop/product/**).
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/shop/product/*/reviews")).permitAll()
                        // Guest checkout: place an order without an account.
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/shop/checkout")).permitAll()
                        // Guest order history lookup by email.
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/shop/orders")).permitAll()
                        // Self-service registration and the dev H2 console are open.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/register/customers/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        // Allow the error dispatch so exceptions on public endpoints render their
                        // real status (e.g. 400/409) instead of being masked as 401.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll()
                        // Admin-only management area.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/show/**")).hasRole("ADMIN")
                        // Everything else (orders, mutations, etc.) requires authentication.
                        .anyRequest().authenticated()
                )
                // No server-side session: each request carries its own JWT.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Reject unauthenticated access with 401 instead of a redirect/403.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authEx) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .authenticationProvider(authenticationProvider())
                // Validate the Bearer token before the username/password filter.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // P0: real password hashing with BCrypt. The previous encoder stored plaintext
    // and its matches() always returned false, which made DB-backed login impossible.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(myUserDetailsService);
        return authenticationProvider;
    }

    // Exposes the AuthenticationManager so /auth/login can verify credentials.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS for local frontend development.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
