package devops.projet_devops.exception;

public class UnknownProductException extends RuntimeException {

    public UnknownProductException(Long productId) {
        super("Unknown product " + productId);
    }
}
