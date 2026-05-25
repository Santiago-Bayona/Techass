package com.techpark.techpark_uq.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Log para depuración
        log.debug("Procesando petición: {}", request.getRequestURI());
        
        try {
            String jwt = obtenerJwtDeLaRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validarToken(jwt)) {
                String email = tokenProvider.obtenerEmailDelToken(jwt);
                
                // Obtener los roles del token
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(tokenProvider.key())
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();
                
                String rolesStr = claims.get("roles", String.class);
                log.info("Roles encontrados en token para {}: {}", email, rolesStr);
                
                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                if (rolesStr != null && !rolesStr.isEmpty()) {
                    authorities = List.of(rolesStr.split(",")).stream()
                            .map(role -> new SimpleGrantedAuthority(role.trim()))
                            .collect(Collectors.toList());
                }
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ Usuario autenticado: {} con roles: {}", email, authorities);
            } else {
                log.debug("No hay token válido para: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("❌ No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    private String obtenerJwtDeLaRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}