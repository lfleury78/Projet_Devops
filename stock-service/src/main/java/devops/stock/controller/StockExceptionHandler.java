package devops.stock.controller;

import java.util.Map;

import devops.stock.service.InsufficientStockException;
import devops.stock.service.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StockExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ProductNotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, String>> handleInsufficient(InsufficientStockException e) {
        return body(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return body(HttpStatus.BAD_REQUEST, e);
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}
