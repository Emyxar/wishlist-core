package ru.emyxar.wishlist_core.unit.services.auth.logout;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.emyxar.wishlist_core.entities.RevokedTokenEntity;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.model.LogoutV1Request;
import ru.emyxar.wishlist_core.repositories.RevokedTokenRepository;
import ru.emyxar.wishlist_core.services.auth.logout.impl.LogoutServiceV1Impl;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceV1Test {

    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String JTI = "test-jti";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final long EXPIRATION_MILLIS = 1_800_000_000_000L;

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Claims claims;

    @InjectMocks
    private LogoutServiceV1Impl logoutService;

    @Test
    @DisplayName("Валидный refresh-токен — jti сохраняется в blacklist")
    void logoutSuccessTest() {
        LogoutV1Request request = new LogoutV1Request(REFRESH_TOKEN);

        mockValidRefreshClaims();
        when(revokedTokenRepository.existsByJti(JTI)).thenReturn(false);

        logoutService.logoutUser(request);

        ArgumentCaptor<RevokedTokenEntity> captor = ArgumentCaptor.forClass(RevokedTokenEntity.class);
        verify(revokedTokenRepository).save(captor.capture());

        RevokedTokenEntity saved = captor.getValue();
        assertEquals(JTI, saved.getJti());
        assertEquals(EXPIRATION_MILLIS / 1000, saved.getExpiredTimestamp());

        verify(jwtUtil).parseClaimsFromToken(REFRESH_TOKEN);
        verify(revokedTokenRepository).existsByJti(JTI);
    }

    @Test
    @DisplayName("Токен уже отозван — повторное сохранение не выполняется (идемпотентность)")
    void logoutAlreadyRevokedTest() {
        LogoutV1Request request = new LogoutV1Request(REFRESH_TOKEN);

        when(jwtUtil.parseClaimsFromToken(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn(JTI);
        when(claims.getSubject()).thenReturn(USER_ID.toString());
        when(revokedTokenRepository.existsByJti(JTI)).thenReturn(true);

        logoutService.logoutUser(request);

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Невалидный или истекший токен — UnauthorizedException из JwtUtil")
    void logoutInvalidTokenTest() {
        LogoutV1Request request = new LogoutV1Request("bad-token");

        when(jwtUtil.parseClaimsFromToken("bad-token"))
                .thenThrow(new UnauthorizedException("Недействительный или истекший токен"));

        assertThrows(
                UnauthorizedException.class,
                () -> logoutService.logoutUser(request)
        );

        verifyNoInteractions(revokedTokenRepository);
    }

    @Test
    @DisplayName("Access-токен вместо refresh — UnauthorizedException")
    void logoutWithAccessTokenTest() {
        LogoutV1Request request = new LogoutV1Request(REFRESH_TOKEN);

        when(jwtUtil.parseClaimsFromToken(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> logoutService.logoutUser(request)
        );
        assertEquals("Невалидный тип токена", exception.getMessage());

        verifyNoInteractions(revokedTokenRepository);
    }

    private void mockValidRefreshClaims() {
        when(jwtUtil.parseClaimsFromToken(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn(JTI);
        when(claims.getExpiration()).thenReturn(new Date(EXPIRATION_MILLIS));
        when(claims.getSubject()).thenReturn(USER_ID.toString());
    }
}
