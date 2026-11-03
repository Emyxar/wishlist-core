package ru.emyxar.wishlist_core.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.emyxar.wishlist_core.exceptions.UnprocessableEntityException;
import ru.emyxar.wishlist_core.utils.ValidatorUtil;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorUtilTest {

    private final ValidatorUtil validatorUtil = new ValidatorUtil();

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_NAME = "Test User";
    private static final String VALID_PASSWORD = "Passw0rd!";

    @Test
    @DisplayName("Корректные данные проходят валидацию без ошибок")
    void validateSuccessTest() {
        assertDoesNotThrow(() -> validatorUtil.validate(
                VALID_EMAIL, VALID_NAME, VALID_PASSWORD, VALID_PASSWORD
        ));
    }

    @Test
    @DisplayName("Невалидный email добавляет ошибку email")
    void validateInvalidEmailTest() {
        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> validatorUtil.validate(
                        "not-an-email", VALID_NAME, VALID_PASSWORD, VALID_PASSWORD
                )
        );

        assertTrue(ex.getErrors().containsKey("email"));
        assertEquals(1, ex.getErrors().size());
    }

    @Test
    @DisplayName("Пустое имя добавляет ошибку fullName")
    void validateEmptyFullNameTest() {
        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> validatorUtil.validate(
                        VALID_EMAIL, "", VALID_PASSWORD, VALID_PASSWORD
                )
        );

        assertTrue(ex.getErrors().containsKey("fullName"));
    }

    @Test
    @DisplayName("Null в имени добавляет ошибку fullName")
    void validateNullFullNameTest() {
        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> validatorUtil.validate(
                        VALID_EMAIL, null, VALID_PASSWORD, VALID_PASSWORD
                )
        );

        assertTrue(ex.getErrors().containsKey("fullName"));
    }

    @Test
    @DisplayName("Слабый пароль добавляет ошибку password")
    void validateWeakPasswordTest() {
        String weakPassword = "password";

        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> validatorUtil.validate(
                        VALID_EMAIL, VALID_NAME, weakPassword, weakPassword
                )
        );

        assertTrue(ex.getErrors().containsKey("password"));
    }

    @Test
    @DisplayName("Несовпадающие пароли добавляют ошибку confirmPassword")
    void validatePasswordMismatchTest() {
        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> validatorUtil.validate(
                        VALID_EMAIL, VALID_NAME, VALID_PASSWORD, "Passw0rd!1"
                )
        );

        assertTrue(ex.getErrors().containsKey("confirmPassword"));
        assertFalse(ex.getErrors().containsKey("password"));
    }

    @Test
    @DisplayName("Несколько невалидных полей собираются в одну ошибку")
    void validateMultipleErrorsTest() {
        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> validatorUtil.validate(
                        "bad-email", "", "weak", "another"
                )
        );

        assertEquals(4, ex.getErrors().size());
        assertTrue(ex.getErrors().containsKey("email"));
        assertTrue(ex.getErrors().containsKey("fullName"));
        assertTrue(ex.getErrors().containsKey("password"));
        assertTrue(ex.getErrors().containsKey("confirmPassword"));
    }
}
