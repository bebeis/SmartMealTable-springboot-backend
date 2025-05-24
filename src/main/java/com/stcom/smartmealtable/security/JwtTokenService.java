package com.stcom.smartmealtable.security;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final MemberRepository memberRepository;
    private final JwtBlacklistService jwtBlacklistService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String createAccessToken(Long memberId, Long profileId) {
        return createToken(String.valueOf(memberId), profileId, 1000 * 60 * 60);
    }

    public String createRefreshToken(Long memberId, Long profileId) {
        return createToken(String.valueOf(memberId), profileId, 1000 * 60 * 60 * 24 * 14);
    }

    private String createToken(String memberId, Long profileId, long expireTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);

        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("email", member.getEmail());
        if (profileId != null) {
            claims.put("profileId", String.valueOf(profileId));
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtTokenResponseDto createTokenDto(Long memberId, Long profileId) {
        return new JwtTokenResponseDto(
                createAccessToken(memberId, profileId),
                createRefreshToken(memberId, profileId),
                3600,
                "Bearer"
        );
    }

    public void validateToken(String token) {
        // Bearer 접두사 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (jwtBlacklistService.isBlacklisted(token)) {
            throw new IllegalArgumentException("블랙리스트에 추가된 토큰으로 접근하였습니다");
        }

        // 토큰 검증
        Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token);
    }

    public Claims extractClaims(String token) {
        // Bearer 접두사 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
