package ru.emyxar.wishlist_core.services.auth.refresh.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.model.RefreshV1Request;
import ru.emyxar.wishlist_core.model.RefreshV1Response;
import ru.emyxar.wishlist_core.repositories.RevokedTokenRepository;
import ru.emyxar.wishlist_core.repositories.UserRepository;
import ru.emyxar.wishlist_core.services.auth.refresh.RefreshTokenServiceV1;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceV1Impl implements RefreshTokenServiceV1 {

    private final RevokedTokenRepository revokedTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public RefreshV1Response refreshToken(RefreshV1Request request) {

        Claims claims = jwtUtil.parseClaimsFromToken(request.getRefreshToken());

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Невалидный тип токена");
        }

        String jti = claims.getId();
        UUID userId = jwtUtil.parseUserId(claims);

        if (revokedTokenRepository.existsByJti(jti)) {
            throw new UnauthorizedException("Недействительный или истекший токен");
        }

        if (!userRepository.existsById(userId)) {
            log.warn("Пользователь {} не найден", userId);
            throw new UnauthorizedException("Недействительный или истекший токен");
        }

        String accessToken = jwtUtil.generateAccessToken(String.valueOf(userId));

        return new RefreshV1Response(accessToken);
    }
}
