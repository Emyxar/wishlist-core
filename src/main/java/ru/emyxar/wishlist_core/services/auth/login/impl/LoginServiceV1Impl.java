package ru.emyxar.wishlist_core.services.auth.login.impl;

import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.model.LoginV1Request;
import ru.emyxar.wishlist_core.model.LoginV1Response;
import ru.emyxar.wishlist_core.services.auth.login.LoginServiceV1;

@Service
public class LoginServiceV1Impl implements LoginServiceV1 {

    @Override
    public LoginV1Response loginUser(LoginV1Request request) {
        return null;
    }
}
