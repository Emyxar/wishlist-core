package ru.emyxar.wishlist_core.exceptions;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.emyxar.wishlist_core.model.ApiError;
import ru.emyxar.wishlist_core.model.ApiErrorResponse;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        Map<String, Object> details = new HashMap<>();
        details.put("request", e.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(400)
                .body(buildApiErrorResponse(
                        "BAD_REQUEST",
                        "Неверный формат запроса или поврежденный JSON",
                        details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
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
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(ResourceAlreadyExistsException e) {
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
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(RuntimeException e) {
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
