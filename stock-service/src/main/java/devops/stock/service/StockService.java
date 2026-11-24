package devops.stock.service;

import java.util.List;
import java.util.Optional;

import devops.stock.model.Product;
import devops.stock.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product save(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public boolean isAvailable(Long id, int quantity) {
        checkPositive(quantity);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return product.getQuantity() >= quantity;
    }

    /** Décrémente le stock d'un produit (appelé lors d'une commande). */
    @Transactional
    public Product reserve(Long id, int quantity) {
        checkPositive(quantity);
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(id, quantity, product.getQuantity());
        }
        product.setQuantity(product.getQuantity() - quantity);
        return productRepository.save(product);
    }

    /** Incrémente le stock d'un produit (réapprovisionnement ou annulation de réservation). */
    @Transactional
    public Product restock(Long id, int quantity) {
        checkPositive(quantity);
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setQuantity(product.getQuantity() + quantity);
        return productRepository.save(product);
    }

    private void checkPositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
