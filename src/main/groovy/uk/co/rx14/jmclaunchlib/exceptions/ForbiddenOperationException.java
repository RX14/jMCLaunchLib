package uk.co.rx14.jmclaunchlib.exceptions;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException() {
        super();
    }

    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenOperationException(Throwable cause) {
        super(cause);
    }

    protected ForbiddenOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
