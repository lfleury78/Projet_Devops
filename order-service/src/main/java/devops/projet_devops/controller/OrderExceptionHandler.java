package devops.projet_devops.controller;

import java.util.Map;

import devops.projet_devops.exception.OutOfStockException;
import devops.projet_devops.exception.StockUnavailableException;
import devops.projet_devops.exception.UnknownProductException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleOutOfStock(OutOfStockException e) {
        return body(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(UnknownProductException.class)
    public ResponseEntity<Map<String, String>> handleUnknownProduct(UnknownProductException e) {
        return body(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(StockUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleStockUnavailable(StockUnavailableException e) {
        return body(HttpStatus.SERVICE_UNAVAILABLE, e);
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}
