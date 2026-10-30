package ru.emyxar.wishlist_core.services.auth.refresh.impl;

import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.model.RefreshV1Request;
import ru.emyxar.wishlist_core.model.RefreshV1Response;
import ru.emyxar.wishlist_core.services.auth.refresh.RefreshTokenServiceV1;

@Service
public class RefreshTokenServiceV1Impl implements RefreshTokenServiceV1 {

    @Override
    public RefreshV1Response refreshToken(RefreshV1Request request) {
        return null;
    }
}
