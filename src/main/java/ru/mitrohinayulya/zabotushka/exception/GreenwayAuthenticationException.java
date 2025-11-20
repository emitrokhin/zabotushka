package ru.mitrohinayulya.zabotushka.exception;

/**
 * Исключение при ошибке аутентификации в MyGreenway
 */
public class GreenwayAuthenticationException extends RuntimeException {

    public GreenwayAuthenticationException(String message) {
        super(message);
    }

    public GreenwayAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
