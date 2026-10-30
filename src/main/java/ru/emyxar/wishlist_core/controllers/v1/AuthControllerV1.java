package ru.emyxar.wishlist_core.controllers.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.emyxar.wishlist_core.api.AuthApi;
import ru.emyxar.wishlist_core.model.*;
import ru.emyxar.wishlist_core.services.auth.login.LoginServiceV1;
import ru.emyxar.wishlist_core.services.auth.logout.LogoutServiceV1;
import ru.emyxar.wishlist_core.services.auth.password.PasswordServiceV1;
import ru.emyxar.wishlist_core.services.auth.refresh.RefreshTokenServiceV1;
import ru.emyxar.wishlist_core.services.auth.registration.RegistrationServiceV1;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class AuthControllerV1 implements AuthApi {

    private final RegistrationServiceV1 registrationService;
    private final LoginServiceV1 loginService;
    private final RefreshTokenServiceV1 refreshTokenService;
    private final LogoutServiceV1 logoutService;
    private final PasswordServiceV1 passwordService;

    @Override
    public ResponseEntity<ApiForgotPasswordV1Response> v1ForgotPasswordApi(
            ForgotPasswordV1Request forgotPasswordV1Request) {
        var response = passwordService.forgotPassword(forgotPasswordV1Request);
        var apiResponse = new ApiForgotPasswordV1Response().status(true).data(response).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<ApiLoginV1Response> v1LoginApi(LoginV1Request loginV1Request) {
        var response = loginService.loginUser(loginV1Request);
        var apiResponse = new ApiLoginV1Response().status(true).data(response).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<ApiLogoutV1Response> v1LogoutApi(LogoutV1Request logoutV1Request) {
        logoutService.logoutUser(logoutV1Request);
        var apiResponse = new ApiLogoutV1Response().status(true).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<ApiRefreshV1Response> v1RefreshApi(RefreshV1Request refreshV1Request) {
        var response = refreshTokenService.refreshToken(refreshV1Request);
        var apiResponse = new ApiRefreshV1Response().status(true).data(response).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<ApiRegisterV1Response> v1RegistrationApi(RegisterV1Request registerV1Request) {
        var response = registrationService.registerUser(registerV1Request);
        var apiResponse = new ApiRegisterV1Response().status(true).data(response).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<ApiResetPasswordV1Response> v1ResetPasswordApi(
            ResetPasswordV1Request resetPasswordV1Request) {
        passwordService.resetPassword(resetPasswordV1Request);
        var apiResponse = new ApiResetPasswordV1Response().status(true).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<ApiVerifyCodeV1Response> v1VerifyCodeApi(VerifyCodeV1Request verifyCodeV1Request) {
        var response = passwordService.verifyCode(verifyCodeV1Request);
        var apiResponse = new ApiVerifyCodeV1Response().status(true).data(response).timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(apiResponse);
    }
}
