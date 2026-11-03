package ru.emyxar.wishlist_core.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.emyxar.wishlist_core.model.ApiError;
import ru.emyxar.wishlist_core.model.ApiErrorResponse;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Map<String, Object> details = new HashMap<>();
        details.put("request", e.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(400)
                .body(buildApiErrorResponse(
                        "BAD_REQUEST",
                        "Неверный формат запроса или поврежденный JSON",
                        details));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<?> handleNotFoundException(HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());

        return ResponseEntity
                .status(404)
                .body(buildApiErrorResponse(
                        "NOT_FOUND",
                        "Запрашиваемый ресурс не найден",
                        details));
    }

    @ExceptionHandler({UnauthorizedException.class, JwtException.class, BadCredentialsException.class,
            InternalAuthenticationServiceException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(RuntimeException e) {
        Map<String, Object> details = new HashMap<>();

        String message = (e instanceof BadCredentialsException)
                ? "Неверный логин или пароль"
                : e.getMessage();

        details.put("reason", message);

        return ResponseEntity
                .status(401)
                .body(buildApiErrorResponse(
                        "UNAUTHORIZED",
                        "Ошибка авторизации",
                        details));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowedException(HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());

        return ResponseEntity
                .status(405)
                .body(buildApiErrorResponse(
                        "METHOD_NOT_ALLOWED",
                        "Метод запроса не поддерживается",
                        details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                details.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity
                .status(422)
                .body(buildApiErrorResponse(
                        "UNPROCESSABLE_ENTITY",
                        "Введенные данные не соответствуют требованиям",
                        details));
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiErrorResponse> handleUnprocessableEntityException(UnprocessableEntityException e) {
        Map<String, Object> details = new HashMap<>(e.getErrors());

        return ResponseEntity
                .status(422)
                .body(buildApiErrorResponse(
                        "UNPROCESSABLE_ENTITY",
                        "Введенные данные не соответствуют требованиям",
                        details));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", e.getMessage());

        return ResponseEntity
                .status(409)
                .body(buildApiErrorResponse(
                        "CONFLICT",
                        "Конфликт при обработке запроса",
                        details));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException e) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", e.getClass().getSimpleName());
        details.put("message", e.getMessage());

        return ResponseEntity
                .status(500)
                .body(buildApiErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "Внутренняя ошибка сервера",
                        details));
    }

    private ApiErrorResponse buildApiErrorResponse(String code, String message, Map<String, Object> details){
        var error = new ApiError();
        error.setCode(code);
        error.setMessage(message);
        error.setDetails(JsonNullable.of(details));

        var apiError = new ApiErrorResponse();
        apiError.setStatus(false);
        apiError.setError(JsonNullable.of(error));
        apiError.setTimestamp(OffsetDateTime.now());

        return apiError;
    }
}
