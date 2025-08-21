package com.neocamp.api_agendamento.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JwtContext {

    private final JwtUtil jwtUtil;

    public JwtContext(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Long getCurrentUserId() {
        String token = getTokenFromRequest();
        if (token != null) {
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("Token JWT não encontrado ou inválido");
    }

    public String getCurrentUserType() {
        String token = getTokenFromRequest();
        if (token != null) {
            return jwtUtil.extractUserType(token);
        }
        throw new RuntimeException("Token JWT não encontrado ou inválido");
    }


    private String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }
}
