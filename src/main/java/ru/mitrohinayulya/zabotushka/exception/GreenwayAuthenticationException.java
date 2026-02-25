package ru.mitrohinayulya.zabotushka.exception;

/// Exception thrown when a MyGreenway authentication error occurs
public class GreenwayAuthenticationException extends RuntimeException {

    public GreenwayAuthenticationException(String message) {
        super(message);
    }
}
