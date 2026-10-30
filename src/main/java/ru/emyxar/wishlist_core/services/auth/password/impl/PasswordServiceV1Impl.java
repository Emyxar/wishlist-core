package ru.emyxar.wishlist_core.services.auth.password.impl;

import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.model.*;
import ru.emyxar.wishlist_core.services.auth.password.PasswordServiceV1;

@Service
public class PasswordServiceV1Impl implements PasswordServiceV1 {

    @Override
    public ForgotPasswordV1Response forgotPassword(ForgotPasswordV1Request request) {
        return null;
    }

    @Override
    public VerifyCodeV1Response verifyCode(VerifyCodeV1Request request) {
        return null;
    }

    @Override
    public void resetPassword(ResetPasswordV1Request request) {
    }
}
