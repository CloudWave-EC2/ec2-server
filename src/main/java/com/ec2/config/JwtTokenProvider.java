package com.ec2.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct; // Spring Framework 6 (Spring Boot 3)
// import javax.annotation.PostConstruct; // Spring Framework 5 (Spring Boot 2)

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest; // Spring Boot 3
// import javax.servlet.http.HttpServletRequest; // Spring Boot 2

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private Key key; // ◀◀◀ Key 타입으로 변경

    private final long tokenValidTime = 60 * 60 * 1000L;

    // ⬇️⬇️⬇️ 이 메소드를 추가해주세요 ⬇️⬇️⬇️
    @PostConstruct
    protected void init() {
        // application.yml의 secretKey 문자열을 UTF-8 바이트 배열로 변환
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA 알고리즘에 적합한 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String userPk) {
        Claims claims = Jwts.claims().setSubject(userPk);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                // ⬇️⬇️⬇️ signWith 부분을 수정해주세요 ⬇️⬇️⬇️
                .signWith(key, SignatureAlgorithm.HS256) // Key 객체와 알고리즘 명시
                .compact();
    }

    // ⬇️⬇️⬇️ 토큰 검증 로직도 Key 객체를 사용하도록 수정 ⬇️⬇️⬇️
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key) // Key 객체로 검증
                    .build()
                    .parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // Key 객체로 파싱
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // resolveToken 메소드는 변경 없습니다.
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}