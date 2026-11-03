package ru.emyxar.wishlist_core.unit.security.filter;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.security.filter.JwtAuthentificationFilter;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthentificationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthentificationFilter jwtAuthentificationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Запрос без заголовка Authorization — ошибка аутентификации")
    void doFilterWithoutAuthorizationHeaderTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Act
        jwtAuthentificationFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("Требуется аутентификация", request.getAttribute("tokenError"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    @DisplayName("Заголовок без префикса Bearer — ошибка аутентификации")
    void doFilterWithNonBearerHeaderTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dGVzdDp0ZXN0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Act
        jwtAuthentificationFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("Требуется аутентификация", request.getAttribute("tokenError"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Валидный Access Token — пользователь аутентифицирован")
    void doFilterWithValidAccessTokenTest() throws Exception {
        String userId = UUID.randomUUID().toString();

        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn(userId);
        when(jwtUtil.parseClaimsFromToken("valid-token")).thenReturn(claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Act
        jwtAuthentificationFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(request.getAttribute("tokenError"));

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userId, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Токен с типом не access — ошибка типа токена")
    void doFilterWithWrongTokenTypeTest() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(jwtUtil.parseClaimsFromToken("refresh-token")).thenReturn(claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer refresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Act
        jwtAuthentificationFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("Невалидный тип токена", request.getAttribute("tokenError"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Невалидный токен — контекст очищается, ошибка сохраняется")
    void doFilterWithInvalidTokenTest() throws Exception {
        when(jwtUtil.parseClaimsFromToken("bad-token"))
                .thenThrow(new UnauthorizedException("Недействительный или истекший токен"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Act
        jwtAuthentificationFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals(
                "Недействительный или истекший токен",
                request.getAttribute("tokenError")
        );
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }
}
