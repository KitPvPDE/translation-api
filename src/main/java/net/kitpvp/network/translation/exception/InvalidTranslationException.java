package net.kitpvp.network.translation.exception;

import java.util.concurrent.ExecutionException;

public class InvalidTranslationException extends ExecutionException {

    public InvalidTranslationException(String message) {
        super(message);
    }

    public InvalidTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
