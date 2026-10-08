package com.mealgram.common.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// JWT 토큰 발급/검증

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 14; // 14일
    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) { 
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(Long memberId) {
        return generateToken(memberId, ACCESS_TOKEN_EXPIRATION, ACCESS_TYPE);
    }

    public String generateRefreshToken(Long memberId) {
        return generateToken(memberId, REFRESH_TOKEN_EXPIRATION, REFRESH_TYPE);
    }

    private String generateToken(Long memberId, long expiration, String type) {

        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TYPE_CLAIM, type)
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(key)
                .compact();
    }

    public Long getMemberId(String token) {

        String subject = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getSubject();
        
        return Long.valueOf(subject);
    }

    public boolean isAccessToken(String token) {
        return hasType(token, ACCESS_TYPE);
    }

    private boolean hasType(String token, String type) {

        try {
            String actual = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .get(TYPE_CLAIM, String.class);

            return type.equals(actual);

        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try { 
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            
                return true;
                
        } catch(Exception e) {
            return false;
        }
    }
    
}
