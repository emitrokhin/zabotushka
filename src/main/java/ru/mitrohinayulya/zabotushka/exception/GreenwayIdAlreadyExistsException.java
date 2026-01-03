package ru.mitrohinayulya.zabotushka.exception;

/**
 * Исключение, возникающее при попытке сохранить пользователя с greenwayId,
 * который уже используется другим пользователем
 */
public class GreenwayIdAlreadyExistsException extends RuntimeException {

    public GreenwayIdAlreadyExistsException(Long greenwayId) {
        super("Greenway ID " + greenwayId + " is already associated with another Telegram account");
    }
}
