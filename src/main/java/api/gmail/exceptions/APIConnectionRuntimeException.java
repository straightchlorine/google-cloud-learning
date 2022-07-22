package api.gmail.exceptions;

/**
 * Custom runtime exception for APIConnection class.
 */
public class APIConnectionRuntimeException extends RuntimeException {
    public APIConnectionRuntimeException(Exception e) {
        super(e);
    }
}
