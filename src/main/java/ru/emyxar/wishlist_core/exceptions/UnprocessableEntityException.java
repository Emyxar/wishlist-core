package ru.emyxar.wishlist_core.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class UnprocessableEntityException extends RuntimeException {
    private final Map<String, String> errors;

    public UnprocessableEntityException(Map<String, String> errors) {
        super("Ошибка валидации данных");
        this.errors = errors;
    }

}
