package application.exceptions;

public class SqlException extends RuntimeException {
    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }
    public SqlException(String message) {
        super(message);
    }
}
