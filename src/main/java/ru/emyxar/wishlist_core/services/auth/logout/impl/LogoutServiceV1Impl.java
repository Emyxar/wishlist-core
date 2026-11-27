package ru.emyxar.wishlist_core.services.auth.logout.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.entities.RevokedTokenEntity;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.model.LogoutV1Request;
import ru.emyxar.wishlist_core.repositories.RevokedTokenRepository;
import ru.emyxar.wishlist_core.services.auth.logout.LogoutServiceV1;
import ru.emyxar.wishlist_core.utils.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutServiceV1Impl implements LogoutServiceV1 {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void logoutUser(LogoutV1Request request) {

        Claims claimsRefresh = jwtUtil.parseClaimsFromToken(request.getRefreshToken());

        if (!"refresh".equals(claimsRefresh.get("type", String.class))) {
            throw new UnauthorizedException("Невалидный тип токена");
        }

        String jtiRefresh = claimsRefresh.getId();

        if (!isRevoked(jtiRefresh)) {
            RevokedTokenEntity tokenEntity = new RevokedTokenEntity();
            tokenEntity.setJti(jtiRefresh);
            tokenEntity.setExpiredTimestamp(claimsRefresh.getExpiration().toInstant().getEpochSecond());
            revokedTokenRepository.save(tokenEntity);
        }

        log.info("Выход из учетной записи: {}", claimsRefresh.getSubject());
    }

    private boolean isRevoked(String jti) {
        return revokedTokenRepository.existsByJti(jti);
    }
}
