package com.mealgram.common.security;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

// JWT 토큰 발급/검증

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 14; // 14일
    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret:}") String secret, Environment environment) {
        this.key = resolveKey(secret, environment);
    }

    public JwtTokenProvider(String secret) {
        this(secret, null);
    }

    private static SecretKey resolveKey(String secret, Environment environment) {
        if (!secret.isBlank()) {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        }
        if (environment != null && environment.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException("JWT_SECRET 환경변수가 필요합니다.");
        }
        log.warn("jwt.secret이 없어 이번 실행에서만 쓰는 임시 키를 만듭니다. 다시 시작하면 발급한 토큰이 모두 무효가 됩니다.");

        return Jwts.SIG.HS256.key().build();
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
                .id(UUID.randomUUID().toString())
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

    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
    }

    public boolean isAccessToken(String token) {
        return hasType(token, ACCESS_TYPE);
    }

    public boolean isRefreshToken(String token) {
        return hasType(token, REFRESH_TYPE);
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

}
