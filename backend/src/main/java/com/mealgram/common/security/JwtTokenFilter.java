package com.mealgram.common.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 요청마다 토큰을 검사하는 필터

@Component 
public class JwtTokenFilter extends OncePerRequestFilter{

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override 
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.isAccessToken(token)) {
            Long memberId = jwtTokenProvider.getMemberId(token);
        
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberId, null, List.of());
        
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }
        
        filterChain.doFilter(request, response);

    }
    
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;

    }



}
