package ru.mitrohinayulya.zabotushka.exception;

/// Exception thrown when a MyGreenway API error occurs
public class GreenwayApiException extends RuntimeException {

    private final String errorCode;
    private final String errorDetail;

    public GreenwayApiException(String message) {
        super(message);
        this.errorCode = null;
        this.errorDetail = null;
    }

    public GreenwayApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorDetail = null;
    }

    public GreenwayApiException(String message, String errorCode, String errorDetail) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    @Override
    public String getMessage() {
        if (errorCode != null || errorDetail != null) {
            return super.getMessage() + " [code: " + errorCode + ", detail: " + errorDetail + "]";
        }
        return super.getMessage();
    }
}
