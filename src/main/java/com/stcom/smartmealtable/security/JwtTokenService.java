package com.stcom.smartmealtable.security;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberRepository;
import com.stcom.smartmealtable.web.dto.token.JwtTokenResponseDto;
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

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String createAccessToken(Long memberId) {
        return createToken(String.valueOf(memberId), 1000 * 60 * 60);
    }

    public String createAccessToken(String memberId) {
        return createToken(memberId, 1000 * 60 * 60);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(String.valueOf(memberId), 1000 * 60 * 60 * 24 * 14);
    }

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

    public JwtTokenResponseDto createTokenDto(Long memberId) {
        return new JwtTokenResponseDto(
                createAccessToken(memberId),
                createRefreshToken(memberId),
                3600,
                "Bearar"
        );
    }

    public String extractMemberIdFromRefreshToken(String refreshToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody()
                    .get("memberId", String.class);
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
        }
    }

    public boolean validateToken(String token) {
        try {
            // Bearer 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 토큰 검증
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Member getClaim(String token) {
        try {
            // Bearer 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String memberIdStr = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("memberId", String.class);

            Long memberId = Long.parseLong(memberIdStr);

            // 회원 정보 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다"));

            return member;
        } catch (Exception e) {
            throw new RuntimeException("토큰 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
