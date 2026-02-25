package ru.mitrohinayulya.zabotushka.exception;

/// Exception thrown when attempting to save a user with a greenwayId
/// that is already associated with another user
public class GreenwayIdAlreadyExistsException extends RuntimeException {

    public GreenwayIdAlreadyExistsException(long greenwayId) {
        super("Greenway ID " + greenwayId + " is already associated with another account");
    }
}
