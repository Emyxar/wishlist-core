package ru.emyxar.wishlist_core.integration;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.repositories.UserRepository;
import ru.emyxar.wishlist_core.utils.JwtUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerV1IntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "Passw0rd!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Регистрация: успешное создание пользователя")
    void registerSuccessTest() throws Exception {
        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest(EMAIL, "Test User", PASSWORD, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(body.get("status").asBoolean());
        assertNotNull(body.get("data").get("id"));

        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        assertEquals("Test User", user.getFullName());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));
    }

    @Test
    @DisplayName("Регистрация: дубликат email — 409 CONFLICT")
    void registerDuplicateEmailTest() throws Exception {
        registerUser(EMAIL, PASSWORD);

        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest(EMAIL, "Test User", PASSWORD, PASSWORD)))
                .andExpect(status().isConflict())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("CONFLICT", error.get("code").asString());
    }

    @Test
    @DisplayName("Регистрация: невалидный email — 422 UNPROCESSABLE_ENTITY")
    void registerInvalidEmailTest() throws Exception {
        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("not-an-email", "Test User", PASSWORD, PASSWORD)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("UNPROCESSABLE_ENTITY", error.get("code").asString());
        assertTrue(error.get("details").has("email"));
    }

    @Test
    @DisplayName("Регистрация: пароли не совпадают — 422 UNPROCESSABLE_ENTITY")
    void registerPasswordMismatchTest() throws Exception {
        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest(EMAIL, "Test User", PASSWORD, "Passw0rd!1")))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("UNPROCESSABLE_ENTITY", error.get("code").asString());
        assertTrue(error.get("details").has("confirmPassword"));
    }

    @Test
    @DisplayName("Логин: успешная аутентификация с выдачей токенов")
    void loginSuccessTest() throws Exception {
        registerUser(EMAIL, PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(body.get("status").asBoolean());

        JsonNode data = body.get("data");
        String userId = data.get("id").asString();
        String accessToken = data.get("accessToken").asString();
        String refreshToken = data.get("refreshToken").asString();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        Claims accessClaims = jwtUtil.parseClaimsFromToken(accessToken);
        assertEquals(userId, accessClaims.getSubject());
        assertEquals("access", accessClaims.get("type", String.class));

        Claims refreshClaims = jwtUtil.parseClaimsFromToken(refreshToken);
        assertEquals(userId, refreshClaims.getSubject());
        assertEquals("refresh", refreshClaims.get("type", String.class));
    }

    @Test
    @DisplayName("Логин: неверный пароль — 401 UNAUTHORIZED")
    void loginWrongPasswordTest() throws Exception {
        registerUser(EMAIL, PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, "Wr0ngPass!")))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("UNAUTHORIZED", error.get("code").asString());
        assertEquals("Неверный логин или пароль", error.get("details").get("reason").asString());
    }

    @Test
    @DisplayName("Логин: несуществующий пользователь — 401 UNAUTHORIZED")
    void loginUnknownEmailTest() throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("unknown@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("UNAUTHORIZED", error.get("code").asString());
        assertEquals("Пользователь не найден", error.get("details").get("reason").asString());
    }

    @Test
    @DisplayName("Защищенный эндпоинт без токена — 401 UNAUTHORIZED")
    void protectedEndpointWithoutTokenTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/wishlists"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("UNAUTHORIZED", error.get("code").asString());
        assertEquals("Требуется аутентификация", error.get("details").get("reason").asString());
    }

    @Test
    @DisplayName("Защищенный эндпоинт с валидным токеном — не 401")
    void protectedEndpointWithTokenTest() throws Exception {
        registerUser(EMAIL, PASSWORD);
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        String accessToken = jwtUtil.generateAccessToken(user.getId().toString());

        // Эндпоинта не существует, но токен валиден — аутентификация проходит,
        // запрос доходит до слоя маршрутизации и падает с 404, а не 401
        mockMvc.perform(get("/api/v1/wishlists")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Защищенный эндпоинт с невалидным токеном — 401 UNAUTHORIZED")
    void protectedEndpointWithInvalidTokenTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/wishlists")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("UNAUTHORIZED", error.get("code").asString());
    }

    @Test
    @DisplayName("Поврежденный JSON в запросе — 400 BAD_REQUEST")
    void malformedJsonTest() throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("BAD_REQUEST", error.get("code").asString());
    }

    @Test
    @DisplayName("Неподдерживаемый HTTP метод — 405 METHOD_NOT_ALLOWED")
    void methodNotAllowedTest() throws Exception {
        MvcResult result = mockMvc.perform(get(LOGIN_URL))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("METHOD_NOT_ALLOWED", error.get("code").asString());
    }

    @Test
    @DisplayName("Несуществующий публичный ресурс — 404 NOT_FOUND")
    void notFoundTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/unknown"))
                .andExpect(status().isNotFound())
                .andReturn();

        JsonNode error = errorOf(result);
        assertEquals("NOT_FOUND", error.get("code").asString());
        assertEquals("/api/v1/auth/unknown", error.get("details").get("path").asString());
    }

    private void registerUser(String email, String password) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPassword(passwordEncoder.encode(password));
        userRepository.saveAndFlush(user);
    }

    private JsonNode errorOf(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertFalse(body.get("status").asBoolean());
        JsonNode error = body.get("error");
        assertNotNull(error);
        return error;
    }

    private String registerRequest(String email, String fullName,
                                   String password, String confirmPassword) {
        return """
                {
                  "email": "%s",
                  "fullName": "%s",
                  "password": "%s",
                  "confirmPassword": "%s"
                }
                """.formatted(email, fullName, password, confirmPassword);
    }

    private String loginRequest(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }
}
