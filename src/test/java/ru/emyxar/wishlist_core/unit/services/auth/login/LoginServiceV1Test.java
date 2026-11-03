package ru.emyxar.wishlist_core.unit.services.auth.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.emyxar.wishlist_core.model.LoginV1Request;
import ru.emyxar.wishlist_core.model.LoginV1Response;
import ru.emyxar.wishlist_core.security.details.UserDetailsImpl;
import ru.emyxar.wishlist_core.services.auth.login.impl.LoginServiceV1Impl;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginServiceV1Test {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginServiceV1Impl loginService;

    @Test
    @DisplayName("Успешная аутентификация пользователя")
    public void loginUserSuccessTest() {
        LoginV1Request request = new LoginV1Request(
                "test@example.com",
                "password"
        );

        UUID userId = UUID.randomUUID();
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(userDetails.getUserId()).thenReturn(userId);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(jwtUtil.generateAccessToken(String.valueOf(userId)))
                .thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(String.valueOf(userId)))
                .thenReturn(refreshToken);

        // Act
        LoginV1Response response = loginService.loginUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(userId.toString(), response.getId());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        verify(authenticationManager).authenticate(
                argThat(auth ->
                        Objects.equals(auth.getPrincipal(), "test@example.com")
                                && Objects.equals(auth.getCredentials(), "password")
                )
        );

        verify(jwtUtil).generateAccessToken(String.valueOf(userId));
        verify(jwtUtil).generateRefreshToken(String.valueOf(userId));
    }

    @Test
    @DisplayName("Неверные учетные данные")
    public void loginUserWrongCredentialsTest() {
        LoginV1Request request = new LoginV1Request(
                "test@example.com",
                "wrong-password"
        );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(
                BadCredentialsException.class,
                () -> loginService.loginUser(request)
        );

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));

        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Ошибка генерации Access Token")
    public void loginUserErrorGenerateAccessToken() {
        LoginV1Request request = new LoginV1Request(
                "test@example.com",
                "password"
        );

        UUID userId = UUID.randomUUID();

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(userDetails.getUserId()).thenReturn(userId);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(jwtUtil.generateAccessToken(userId.toString()))
                .thenThrow(new RuntimeException("JWT generation failed"));

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> loginService.loginUser(request)
        );

        verify(jwtUtil).generateAccessToken(userId.toString());

        verify(jwtUtil, never())
                .generateRefreshToken(anyString());
    }

    @Test
    @DisplayName("Ошибка генерации Refresh Token")
    public void loginUserErrorGenerateRefreshToken() {
        LoginV1Request request = new LoginV1Request(
                "test@example.com",
                "password"
        );

        UUID userId = UUID.randomUUID();

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(userDetails.getUserId()).thenReturn(userId);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(jwtUtil.generateRefreshToken(userId.toString()))
                .thenThrow(new RuntimeException("JWT generation failed"));

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> loginService.loginUser(request)
        );

        verify(jwtUtil).generateAccessToken(userId.toString());
        verify(jwtUtil).generateRefreshToken(userId.toString());
    }
}
