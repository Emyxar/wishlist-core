package ru.emyxar.wishlist_core.services.auth.refresh;

import ru.emyxar.wishlist_core.model.RefreshV1Request;
import ru.emyxar.wishlist_core.model.RefreshV1Response;

public interface RefreshTokenServiceV1 {
    RefreshV1Response refreshToken(RefreshV1Request request);
}
