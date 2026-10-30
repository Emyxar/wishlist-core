package ru.emyxar.wishlist_core.utils;

public class Constants {
    public static String REGEX_EMAIL_PATTERN =
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    public static String REGEX_PASSWORD_PATTERN =
            "^(?=.*[\\p{L}])(?=.*\\d)(?=.*[!@#$%^&*])[\\p{L}\\d!@#$%^&*]{8,128}$";
}
