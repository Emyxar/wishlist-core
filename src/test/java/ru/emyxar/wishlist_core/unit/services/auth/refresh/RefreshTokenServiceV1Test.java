package ru.emyxar.wishlist_core.unit.services.auth.refresh;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.model.RefreshV1Request;
import ru.emyxar.wishlist_core.model.RefreshV1Response;
import ru.emyxar.wishlist_core.repositories.RevokedTokenRepository;
import ru.emyxar.wishlist_core.repositories.UserRepository;
import ru.emyxar.wishlist_core.services.auth.refresh.impl.RefreshTokenServiceV1Impl;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceV1Test {

    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String ACCESS_TOKEN = "new-access-token";
    private static final String JTI = "test-jti";
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Claims claims;

    @InjectMocks
    private RefreshTokenServiceV1Impl refreshTokenService;

    @Test
    @DisplayName("Валидный refresh-токен — выдача нового access-токена")
    void refreshTokenSuccessTest() {
        RefreshV1Request request = new RefreshV1Request(REFRESH_TOKEN);

        mockValidRefreshClaims();
        when(revokedTokenRepository.existsByJti(JTI)).thenReturn(false);
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(jwtUtil.generateAccessToken(String.valueOf(USER_ID))).thenReturn(ACCESS_TOKEN);

        RefreshV1Response response = refreshTokenService.refreshToken(request);

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getAccessToken());

        verify(jwtUtil).parseClaimsFromToken(REFRESH_TOKEN);
        verify(revokedTokenRepository).existsByJti(JTI);
        verify(userRepository).existsById(USER_ID);
        verify(jwtUtil).generateAccessToken(String.valueOf(USER_ID));
    }

    @Test
    @DisplayName("Access-токен вместо refresh — UnauthorizedException")
    void refreshTokenWrongTypeTest() {
        RefreshV1Request request = new RefreshV1Request(REFRESH_TOKEN);

        when(jwtUtil.parseClaimsFromToken(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.refreshToken(request)
        );
        assertEquals("Невалидный тип токена", exception.getMessage());

        verifyNoInteractions(revokedTokenRepository, userRepository);
        verify(jwtUtil, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Невалидный или истекший токен — UnauthorizedException из JwtUtil")
    void refreshTokenInvalidTokenTest() {
        RefreshV1Request request = new RefreshV1Request("bad-token");

        when(jwtUtil.parseClaimsFromToken("bad-token"))
                .thenThrow(new UnauthorizedException("Недействительный или истекший токен"));

        assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.refreshToken(request)
        );

        verifyNoInteractions(revokedTokenRepository, userRepository);
        verify(jwtUtil, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Отозванный refresh-токен — UnauthorizedException")
    void refreshTokenRevokedTest() {
        RefreshV1Request request = new RefreshV1Request(REFRESH_TOKEN);

        mockValidRefreshClaims();
        when(revokedTokenRepository.existsByJti(JTI)).thenReturn(true);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.refreshToken(request)
        );
        assertEquals("Недействительный или истекший токен", exception.getMessage());

        verifyNoInteractions(userRepository);
        verify(jwtUtil, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Пользователь из токена не найден — UnauthorizedException")
    void refreshTokenUserNotFoundTest() {
        RefreshV1Request request = new RefreshV1Request(REFRESH_TOKEN);

        mockValidRefreshClaims();
        when(revokedTokenRepository.existsByJti(JTI)).thenReturn(false);
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.refreshToken(request)
        );
        assertEquals("Недействительный или истекший токен", exception.getMessage());

        verify(jwtUtil, never()).generateAccessToken(anyString());
    }

    private void mockValidRefreshClaims() {
        when(jwtUtil.parseClaimsFromToken(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn(JTI);
        when(jwtUtil.parseUserId(claims)).thenReturn(USER_ID);
    }
}
