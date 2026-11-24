package devops.projet_devops.exception;

public class StockUnavailableException extends RuntimeException {

    public StockUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
