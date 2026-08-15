package com.stockmonitor.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private SecretKey accessSigningKey;
    private SecretKey refreshSigningKey;

    @Value("${jwt.expiration.access}")
    private long accessTokenValidityInMinutes;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenValidityInDays;

    @Value("${jwt.secret.access}")
    public void setAccessTokenSecret(String secret) {
        // When generating the key, you can specify the algorithm you want to use.
        // Keys.hmacShaKeyFor creates a key suitable for HS256/384/512 based on key length.
        this.accessSigningKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Value("${jwt.secret.refresh}")
    public void setRefreshTokenSecret(String secret) {
        this.refreshSigningKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + accessTokenValidityInMinutes * 60 * 1000); // Minutes to milliseconds

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(accessSigningKey) // <-- Removed SignatureAlgorithm.HS256
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + refreshTokenValidityInDays * 24 * 60 * 60 * 1000); // Days to milliseconds

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(refreshSigningKey) // <-- Removed SignatureAlgorithm.HS256
                .compact();
    }

    public Authentication getAuthenticationFromAccessToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(accessSigningKey) // No change needed here for parsing
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth", String.class).split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateAccessToken(String authToken) {
        try {
            // No change needed here for validation
            Jwts.parserBuilder().setSigningKey(accessSigningKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            // No change needed here for validation
            Jwts.parserBuilder().setSigningKey(refreshSigningKey).build().parseClaimsJws(refreshToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid Refresh JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid Refresh JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired Refresh JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported Refresh JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Refresh JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public String getUsernameFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(refreshSigningKey) // No change needed here for parsing
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error getting username from refresh token: {}", e.getMessage());
            return null;
        }
    }
}