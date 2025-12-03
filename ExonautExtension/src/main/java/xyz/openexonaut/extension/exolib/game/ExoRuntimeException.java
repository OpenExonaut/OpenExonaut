package xyz.openexonaut.extension.exolib.game;

public class ExoRuntimeException extends RuntimeException {
    public ExoRuntimeException(String message) {
        super(message);
    }

    public ExoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
