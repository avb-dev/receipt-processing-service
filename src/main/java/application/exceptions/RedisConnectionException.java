package application.exceptions;

public class RedisConnectionException extends RuntimeException {
    public RedisConnectionException(String message, Exception cause) {
        super(message, cause);
    }
    public RedisConnectionException(String message) {
        super(message);
    }
}
