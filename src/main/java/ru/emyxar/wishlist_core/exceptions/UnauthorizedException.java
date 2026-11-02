package ru.emyxar.wishlist_core.exceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class UnauthorizedException extends RuntimeException {

    @Autowired
    public UnauthorizedException(String message) {
        super(message);
    }
}
