package ru.emyxar.wishlist_core.security.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.emyxar.wishlist_core.model.ApiError;
import ru.emyxar.wishlist_core.model.ApiErrorResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AuthenticationEntryPointImp implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(401);

        Object attr = request.getAttribute("tokenError");
        String errorMessage = (attr != null)
                ? attr.toString()
                : e.getMessage();

        Map<String, Object> details = new HashMap<>();
        details.put("reason", errorMessage);

        var apiError = new ApiError();
        apiError.setCode("UNAUTHORIZED");
        apiError.setMessage("Ошибка авторизации");
        apiError.setDetails(JsonNullable.of(details));

        var apiErrorResponse = new ApiErrorResponse();
        apiErrorResponse.setStatus(false);
        apiErrorResponse.setError(JsonNullable.of(apiError));
        apiErrorResponse.setTimestamp(OffsetDateTime.now());

        response.getWriter().write(new ObjectMapper().writeValueAsString(apiErrorResponse));
    }
}
