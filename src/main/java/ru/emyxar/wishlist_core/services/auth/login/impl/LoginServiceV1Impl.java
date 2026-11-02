package ru.emyxar.wishlist_core.services.auth.login.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.model.LoginV1Request;
import ru.emyxar.wishlist_core.model.LoginV1Response;
import ru.emyxar.wishlist_core.security.details.UserDetailsImpl;
import ru.emyxar.wishlist_core.services.auth.login.LoginServiceV1;
import ru.emyxar.wishlist_core.utils.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceV1Impl implements LoginServiceV1 {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginV1Response loginUser(LoginV1Request request) {
        Authentication authentication = authenticate(request);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String userId = String.valueOf(userDetails.getUserId());
        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        log.info("Пользователь {} успешно прошел аутентификацию", userId);
        return new LoginV1Response(userId, accessToken, refreshToken);
    }

    private Authentication authenticate(LoginV1Request request){
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
    }
}
