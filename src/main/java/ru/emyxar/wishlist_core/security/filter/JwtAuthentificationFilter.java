package ru.emyxar.wishlist_core.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.utils.JwtUtil;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthentificationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            request.setAttribute("tokenError", "Требуется аутентификация");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtUtil.parseClaimsFromToken(token);

            if (!"access".equals(claims.get("type", String.class))) {
                request.setAttribute("tokenError", "Невалидный тип токена");
                filterChain.doFilter(request, response);
                return;
            }
            String userId = claims.getSubject();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (JwtException | AuthenticationException | IllegalArgumentException | UnauthorizedException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute("tokenError", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
