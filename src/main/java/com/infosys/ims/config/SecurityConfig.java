package com.infosys.ims.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize on every controller method
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── PUBLIC ────────────────────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/suppliers/register",      // supplier self-registration
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ── ADMIN-ONLY management endpoints ───────────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── INVENTORY: strict isolation ───────────────────────────────
                        // /inventory                → ADMIN only (all warehouses)
                        // /inventory/low-stock      → ADMIN only (global low stock)
                        // /inventory/my-warehouse** → MANAGER + STAFF (their own warehouse)
                        // /inventory/my-warehouse/low-stock → MANAGER only (not staff)
                        .requestMatchers(HttpMethod.GET, "/api/inventory").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inventory/low-stock").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inventory/my-warehouse/low-stock").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/inventory/my-warehouse/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/inventory/my-warehouse").hasAnyRole("MANAGER", "STAFF")

                        // ── STOCK MOVEMENTS: strict isolation ─────────────────────────
                        // Global queries (all warehouses)  → ADMIN only
                        // my-warehouse queries             → MANAGER only
                        // Staff has NO direct movement endpoint (uses reports)
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/product/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/warehouse/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/reference/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/my-warehouse/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/my-warehouse").hasRole("MANAGER")

                        // ── WAREHOUSES ────────────────────────────────────────────────
                        // ADMIN: full CRUD
                        // MANAGER + STAFF: read their own warehouse (via /my-warehouse in inventory)
                        //   — they don't need a /warehouses list endpoint
                        .requestMatchers(HttpMethod.POST, "/api/warehouses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/warehouses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/warehouses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/warehouses/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")

                        // ── CATEGORIES ────────────────────────────────────────────────
                        // ADMIN: full CRUD
                        // MANAGER, STAFF, SUPPLIER: read-only
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/categories/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF", "SUPPLIER")

                        // ── PRODUCTS ──────────────────────────────────────────────────
                        // ADMIN: full CRUD + set preferred supplier + remove supplier link
                        // MANAGER: read-only + set preferred supplier + remove supplier link
                        // STAFF: read-only (their warehouse context)
                        // SUPPLIER: browse their categories, link products, update price
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}/activate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/*/preferred-supplier/*")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/supplier-links/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/products/my-categories").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/api/products/my-linked").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/api/products/*/link").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/supplier-links/*/price").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF", "SUPPLIER")

                        // ── PURCHASE ORDERS ───────────────────────────────────────────
                        // ADMIN: read all, cancel any
                        // MANAGER: create (own warehouse), send (own warehouse), cancel (own warehouse), read own
                        // STAFF: receive (own warehouse), read own warehouse's POs (via my-warehouse)
                        // SUPPLIER: accept, reject, ship, read their own POs
                        .requestMatchers(HttpMethod.POST, "/api/purchase-orders").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/send").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/receive").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/cancel")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/status/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/my-warehouse").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/supplier/**").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/accept").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/reject").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/ship").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF", "SUPPLIER")

                        // ── STOCK ISSUES ──────────────────────────────────────────────
                        // STAFF: create, add items, remove items, cancel, execute issue
                        // MANAGER: approve, reject, view all in his warehouse
                        .requestMatchers(HttpMethod.POST, "/api/stock-issues").hasRole("STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/stock-issues/*/items").hasRole("STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/stock-issues/*/items/*").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/stock-issues/*/cancel").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/stock-issues/*/issue").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/stock-issues/*/approve").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/stock-issues/*/reject").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-issues/warehouse/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-issues/my-issues").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/stock-issues/dashboard").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/stock-issues/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF")

                        // ── SUPPLIERS ─────────────────────────────────────────────────
                        // Public: register
                        // SUPPLIER: complete profile, view profile, dashboard
                        // ADMIN: list all, pending, approve, reject
                        // MANAGER: list, view by id
                        .requestMatchers(HttpMethod.POST, "/api/suppliers/profile").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/profile").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/dashboard").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/*/reject").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // ── REPORTS: strict per-role isolation ───────────────────────
                        // Admin reports: /api/reports/admin/**
                        // Manager reports: /api/reports/manager/**
                        // Staff reports: /api/reports/staff/**
                        // Supplier reports: /api/reports/supplier/**
                        .requestMatchers("/api/reports/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/manager/**").hasRole("MANAGER")
                        .requestMatchers("/api/reports/staff/**").hasRole("STAFF")
                        .requestMatchers("/api/reports/supplier/**").hasRole("SUPPLIER")

                        // ── MANAGER dashboard ─────────────────────────────────────────
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")

                        // ── Dashboards ────────────────────────────────────────────────
                        .requestMatchers("/api/dashboard/**").authenticated()

                        // ── Anything else requires authentication ─────────────────────
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:4300"

        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}