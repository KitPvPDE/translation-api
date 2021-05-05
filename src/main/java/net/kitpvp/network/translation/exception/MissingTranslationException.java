package net.kitpvp.network.translation.exception;

import java.util.concurrent.ExecutionException;

public class MissingTranslationException extends ExecutionException {

    public MissingTranslationException(String message) {
        super(message);
    }

    public MissingTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
