package devops.stock;

import java.util.Optional;

import devops.stock.model.Product;
import devops.stock.repository.ProductRepository;
import devops.stock.service.InsufficientStockException;
import devops.stock.service.ProductNotFoundException;
import devops.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockService stockService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Clavier", 10);
        product.setId(1L);
    }

    @Test
    @DisplayName("save() refuse un nom vide")
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> stockService.save(new Product(" ", 1)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("save() refuse une quantité négative")
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> stockService.save(new Product("X", -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("save() enregistre un produit valide")
    void shouldSaveValidProduct() {
        when(productRepository.save(product)).thenReturn(product);

        assertThat(stockService.save(product)).isSameAs(product);
    }

    @Test
    @DisplayName("isAvailable() compare la quantité demandée au stock")
    void shouldCheckAvailability() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThat(stockService.isAvailable(1L, 10)).isTrue();
        assertThat(stockService.isAvailable(1L, 11)).isFalse();
    }

    @Test
    @DisplayName("isAvailable() lève ProductNotFoundException si le produit n'existe pas")
    void shouldThrowWhenProductUnknown() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.isAvailable(9L, 1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("reserve() décrémente le stock")
    void shouldDecrementStock() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(stockService.reserve(1L, 4).getQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("reserve() autorise de vider exactement le stock")
    void shouldAllowReservingAllStock() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(stockService.reserve(1L, 10).getQuantity()).isZero();
    }

    @Test
    @DisplayName("reserve() lève InsufficientStockException si le stock est insuffisant")
    void shouldRejectWhenInsufficient() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockService.reserve(1L, 11))
                .isInstanceOf(InsufficientStockException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserve() lève ProductNotFoundException si le produit n'existe pas")
    void shouldRejectUnknownProductOnReserve() {
        when(productRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.reserve(9L, 1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("reserve() et restock() refusent une quantité <= 0")
    void shouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> stockService.reserve(1L, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> stockService.restock(1L, -2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("restock() incrémente le stock")
    void shouldIncrementStock() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(stockService.restock(1L, 5).getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("restock() lève ProductNotFoundException si le produit n'existe pas")
    void shouldRejectUnknownProductOnRestock() {
        when(productRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.restock(9L, 1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("findAll(), findById() et deleteById() délèguent au repository")
    void shouldDelegateToRepository() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThat(stockService.findById(1L)).contains(product);
        stockService.findAll();
        stockService.deleteById(1L);

        verify(productRepository).findAll();
        verify(productRepository).deleteById(1L);
    }
}
