package ru.emyxar.wishlist_core.services.auth.login;

import ru.emyxar.wishlist_core.model.LoginV1Request;
import ru.emyxar.wishlist_core.model.LoginV1Response;

public interface LoginServiceV1 {
    LoginV1Response loginUser(LoginV1Request request);
}
