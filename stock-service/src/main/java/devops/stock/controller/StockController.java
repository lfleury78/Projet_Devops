package devops.stock.controller;

import java.util.List;

import devops.stock.model.Product;
import devops.stock.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock/products")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<Product> findAll() {
        return stockService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        return stockService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        product.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (stockService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        stockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/availability")
    public boolean isAvailable(@PathVariable Long id, @RequestParam int quantity) {
        return stockService.isAvailable(id, quantity);
    }

    @PostMapping("/{id}/reserve")
    public Product reserve(@PathVariable Long id, @RequestParam int quantity) {
        return stockService.reserve(id, quantity);
    }

    @PostMapping("/{id}/restock")
    public Product restock(@PathVariable Long id, @RequestParam int quantity) {
        return stockService.restock(id, quantity);
    }
}
