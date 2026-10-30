package ru.emyxar.wishlist_core.services.auth.logout;

import ru.emyxar.wishlist_core.model.LogoutV1Request;

public interface LogoutServiceV1 {
    void logoutUser(LogoutV1Request request);
}
