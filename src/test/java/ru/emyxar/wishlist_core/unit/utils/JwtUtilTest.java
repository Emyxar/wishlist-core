package ru.emyxar.wishlist_core.unit.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET =
            "test-secret-key-long-enough-for-hmac-sha-256-algorithm-0123456789";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 600_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 2_592_000_000L);
        ReflectionTestUtils.setField(jwtUtil, "resetPasswordTokenExpiration", 300_000L);
        jwtUtil.init();
    }

    @Test
    @DisplayName("Генерация Access Token с корректными claims")
    void generateAccessTokenSuccessTest() {
        String userId = UUID.randomUUID().toString();

        String token = jwtUtil.generateAccessToken(userId);
        Claims claims = jwtUtil.parseClaimsFromToken(token);

        assertNotNull(token);
        assertEquals(userId, claims.getSubject());
        assertEquals("access", claims.get("type", String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }

    @Test
    @DisplayName("Генерация Refresh Token с корректными claims")
    void generateRefreshTokenSuccessTest() {
        String userId = UUID.randomUUID().toString();

        String token = jwtUtil.generateRefreshToken(userId);
        Claims claims = jwtUtil.parseClaimsFromToken(token);

        assertNotNull(token);
        assertEquals(userId, claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
    }

    @Test
    @DisplayName("Парсинг невалидного токена выбрасывает UnauthorizedException")
    void parseInvalidTokenThrowsUnauthorizedTest() {
        assertThrows(
                UnauthorizedException.class,
                () -> jwtUtil.parseClaimsFromToken("invalid.token.here")
        );
    }

    @Test
    @DisplayName("Парсинг токена, подписанного другим ключом, выбрасывает UnauthorizedException")
    void parseTokenSignedWithAnotherKeyThrowsUnauthorizedTest() {
        String otherSecret =
                "another-secret-key-long-enough-for-hmac-sha-256-algorithm-987654321";

        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("type", "access")
                .signWith(Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(
                UnauthorizedException.class,
                () -> jwtUtil.parseClaimsFromToken(token)
        );
    }

    @Test
    @DisplayName("Парсинг истекшего токена выбрасывает UnauthorizedException")
    void parseExpiredTokenThrowsUnauthorizedTest() {
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -60_000L);

        String expiredToken = jwtUtil.generateAccessToken(UUID.randomUUID().toString());

        assertThrows(
                UnauthorizedException.class,
                () -> jwtUtil.parseClaimsFromToken(expiredToken)
        );
    }
}
