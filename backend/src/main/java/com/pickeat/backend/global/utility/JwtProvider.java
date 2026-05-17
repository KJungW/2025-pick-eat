package com.pickeat.backend.global.utility;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey secretKey;

    public JwtProvider(
            @Value("${jwt.secretKey}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public TokenResponse createToken(Object id, Long expirationMillis) {
        String rawToken = createRawToken(Jwts.builder(), id, expirationMillis);
        return TokenResponse.from(rawToken);
    }

    public TokenResponse createTokenWithClaims(
            Object id,
            Long expirationMillis,
            Map<String, Object> extraClaims
    ) {
        JwtBuilder builder = registerClaims(Jwts.builder(), extraClaims);
        String rawToken = createRawToken(builder, id, expirationMillis);
        return TokenResponse.from(rawToken);
    }

    public Claims getClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new ClientException(ClientErrorCode.TOKEN_IS_EMPTY);
        }
        return extrudeClaimsInToken(token);
    }

    private JwtBuilder registerClaims(
            JwtBuilder builder,
            Map<String, Object> rawClaims
    ) {
        if (rawClaims == null || rawClaims.isEmpty()) {
            return builder;
        }
        Map<String, Object> safeClaims = new HashMap<>(rawClaims);
        safeClaims.keySet().removeAll(Set.of("sub", "iat", "exp"));
        safeClaims.forEach(builder::claim);
        return builder;
    }

    private String createRawToken(
            JwtBuilder builder,
            Object id,
            Long expirationMillis
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);
        return builder
                .subject(String.valueOf(id))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    private Claims extrudeClaimsInToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new ClientException(ClientErrorCode.EXPIRED_TOKEN, e);
        } catch (SecurityException | JwtException e) {
            throw new ClientException(ClientErrorCode.INVALID_TOKEN, e);
        }
    }
}
