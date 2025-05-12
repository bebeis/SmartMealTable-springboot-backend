package com.stcom.smartmealtable.domain.member;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    // Access 토큰 생성 (만료 시간: 1시간)
    public String createAccessToken(String memberId) {
        return createToken(memberId, 1000 * 60 * 60);
    }
    
    // Refresh 토큰 생성 (만료 시간: 2주)
    public String createRefreshToken(String memberId) {
        return createToken(memberId, 1000 * 60 * 60 * 24 * 14);
    }
    
    // JWT 토큰 생성 공통 메서드
    private String createToken(String memberId, long expireTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
