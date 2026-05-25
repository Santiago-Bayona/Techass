package com.techpark.techpark_uq.config;

import com.techpark.techpark_uq.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ========== ENDPOINTS PÚBLICOS ==========
                        .requestMatchers("/api/auth/login", "/api/auth/registro", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/atracciones", "/api/atracciones/**", "/api/zonas", "/api/zonas/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // ========== ENDPOINTS PROTEGIDOS POR ROL ==========
                        // Solo Administradores
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/reportes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/clima/activar/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/clima/desactivar/**").hasRole("ADMINISTRADOR")
                        
                        // Operadores y Administradores
                        .requestMatchers("/api/mantenimiento/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                        .requestMatchers("/api/colas/siguiente/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                        
                        // Visitantes
                        .requestMatchers("/api/colas/unirse").hasRole("VISITANTE")
                        .requestMatchers("/api/colas/cancelar/**").hasRole("VISITANTE")
                        .requestMatchers("/api/colas/posicion/**").hasRole("VISITANTE")
                        
                        // Usuarios autenticados (cualquier usuario logueado)
                        .requestMatchers("/api/colas/estado/**").authenticated()
                        .requestMatchers("/api/rutas/**").authenticated()
                        .requestMatchers("/api/usuarios/perfil/**").authenticated()
                        .requestMatchers("/api/usuarios/saldo/**").authenticated()
                        .requestMatchers("/api/usuarios/recargar").authenticated()
                        
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
            "http://localhost:4200",
            "http://localhost:3000",
            "http://127.0.0.1:4200",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
            "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}