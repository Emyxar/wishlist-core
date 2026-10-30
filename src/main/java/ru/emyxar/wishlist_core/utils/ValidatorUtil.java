package ru.emyxar.wishlist_core.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.emyxar.wishlist_core.exceptions.UnprocessableEntityException;
import ru.emyxar.wishlist_core.model.RegisterV1Request;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static ru.emyxar.wishlist_core.utils.Constants.REGEX_EMAIL_PATTERN;
import static ru.emyxar.wishlist_core.utils.Constants.REGEX_PASSWORD_PATTERN;

@Component
public class ValidatorUtil {

    public void validate(String email, String fullName, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();
        if (!isValidEmail(email)) {
            errors.put("email", "Неверный формат адреса электронной почты. Укажите действительный адрес, например, user@example.com");
        }
        if (!isValidFullName(fullName)) {
            errors.put("fullName", "Некорректно заполнено имя. Имя не может быть пустым");
        }
        if (!checkPassword(password)) {
            errors.put("password", "Некорректный пароль. Пароль должен содержать минимум 8 символов, включая буквы, цифры и специальные символы (например, !@#$)");
        }
        if (!password.equals(confirmPassword) ){
            errors.put("confirmPassword", "Пароли не совпадают");
        }

        if (!errors.isEmpty()){
            throw new UnprocessableEntityException(errors);
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile(REGEX_EMAIL_PATTERN).matcher(email).matches();
    }

    private boolean isValidFullName(String fullName) {
        return fullName != null && fullName.length() <= 254 && !fullName.isEmpty();
    }

    private boolean checkPassword(String password) {
        return Pattern.compile(REGEX_PASSWORD_PATTERN).matcher(password).matches();
    }
}
