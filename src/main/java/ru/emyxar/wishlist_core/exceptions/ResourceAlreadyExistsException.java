package ru.emyxar.wishlist_core.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
