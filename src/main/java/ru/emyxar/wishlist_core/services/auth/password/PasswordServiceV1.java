package ru.emyxar.wishlist_core.services.auth.password;

import ru.emyxar.wishlist_core.model.*;

public interface PasswordServiceV1 {
    ForgotPasswordV1Response forgotPassword(ForgotPasswordV1Request request);
    VerifyCodeV1Response verifyCode(VerifyCodeV1Request request);
    void resetPassword(ResetPasswordV1Request request);
}
