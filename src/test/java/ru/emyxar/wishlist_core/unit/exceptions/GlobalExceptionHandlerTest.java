package ru.emyxar.wishlist_core.unit.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.emyxar.wishlist_core.exceptions.GlobalExceptionHandler;
import ru.emyxar.wishlist_core.exceptions.ResourceAlreadyExistsException;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.exceptions.UnprocessableEntityException;
import ru.emyxar.wishlist_core.model.ApiError;
import ru.emyxar.wishlist_core.model.ApiErrorResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BadCredentialsException — 401 с сообщением о неверном логине или пароле")
    void handleBadCredentialsExceptionTest() {
        ResponseEntity<ApiErrorResponse> response = handler
                .handleUnauthorizedException(new BadCredentialsException("Bad credentials"));

        ApiError error = assertErrorResponse(response, 401, "UNAUTHORIZED");
        assertEquals("Неверный логин или пароль", error.getDetails().get().get("reason"));
    }

    @Test
    @DisplayName("UnauthorizedException — 401 с исходным сообщением")
    void handleUnauthorizedExceptionTest() {
        ResponseEntity<ApiErrorResponse> response = handler
                .handleUnauthorizedException(new UnauthorizedException("Токен истек"));

        ApiError error = assertErrorResponse(response, 401, "UNAUTHORIZED");
        assertEquals("Токен истек", error.getDetails().get().get("reason"));
    }

    @Test
    @DisplayName("UnprocessableEntityException — 422 с картой ошибок валидации")
    void handleUnprocessableEntityExceptionTest() {
        Map<String, String> errors = Map.of("email", "Неверный формат адреса");

        ResponseEntity<ApiErrorResponse> response = handler
                .handleUnprocessableEntityException(new UnprocessableEntityException(errors));

        ApiError error = assertErrorResponse(response, 422, "UNPROCESSABLE_ENTITY");
        assertEquals("Неверный формат адреса", error.getDetails().get().get("email"));
    }

    @Test
    @DisplayName("Ошибка валидации аргументов — 422 с ошибками полей")
    void handleMethodArgumentNotValidExceptionTest() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "email", "Не должно быть пустым")
        ));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleMethodArgumentNotValidException(exception);

        ApiError error = assertErrorResponse(response, 422, "UNPROCESSABLE_ENTITY");
        assertEquals("Не должно быть пустым", error.getDetails().get().get("email"));
    }

    @Test
    @DisplayName("ResourceAlreadyExistsException — 409 с причиной конфликта")
    void handleResourceAlreadyExistsExceptionTest() {
        ResponseEntity<ApiErrorResponse> response = handler
                .handleResourceAlreadyExistsException(
                        new ResourceAlreadyExistsException("Пользователь уже существует"));

        ApiError error = assertErrorResponse(response, 409, "CONFLICT");
        assertEquals("Пользователь уже существует", error.getDetails().get().get("reason"));
    }

    @Test
    @DisplayName("Необработанное исключение — 500 с типом и сообщением")
    void handleRuntimeExceptionTest() {
        ResponseEntity<ApiErrorResponse> response = handler
                .handleRuntimeException(new IllegalStateException("Что-то пошло не так"));

        ApiError error = assertErrorResponse(response, 500, "INTERNAL_SERVER_ERROR");
        assertEquals("IllegalStateException", error.getDetails().get().get("exception"));
        assertEquals("Что-то пошло не так", error.getDetails().get().get("message"));
    }

    @Test
    @DisplayName("Ресурс не найден — 404 с путем запроса")
    void handleNotFoundExceptionTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/unknown");

        ResponseEntity<?> response = handler.handleNotFoundException(request);

        assertEquals(404, response.getStatusCode().value());

        ApiErrorResponse body = (ApiErrorResponse) response.getBody();
        assertNotNull(body);
        ApiError error = body.getError().get();
        assertEquals("NOT_FOUND", error.getCode());
        assertEquals("/api/v1/unknown", error.getDetails().get().get("path"));
    }

    private ApiError assertErrorResponse(ResponseEntity<ApiErrorResponse> response,
                                         int expectedStatus,
                                         String expectedCode) {
        assertEquals(expectedStatus, response.getStatusCode().value());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.getStatus());
        assertNotNull(body.getTimestamp());

        ApiError error = body.getError().get();
        assertEquals(expectedCode, error.getCode());
        assertNotNull(error.getMessage());
        assertNotNull(error.getDetails());
        return error;
    }
}
