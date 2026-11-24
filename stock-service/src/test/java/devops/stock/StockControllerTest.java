package devops.stock;

import java.util.List;
import java.util.Optional;

import devops.stock.controller.StockController;
import devops.stock.model.Product;
import devops.stock.service.InsufficientStockException;
import devops.stock.service.ProductNotFoundException;
import devops.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Clavier", 10);
        product.setId(1L);
    }

    @Test
    @DisplayName("GET /api/stock/products retourne la liste")
    void shouldListProducts() throws Exception {
        when(stockService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/stock/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Clavier"));
    }

    @Test
    @DisplayName("GET /api/stock/products/{id} retourne 200 puis 404")
    void shouldFindById() throws Exception {
        when(stockService.findById(1L)).thenReturn(Optional.of(product));
        when(stockService.findById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stock/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
        mockMvc.perform(get("/api/stock/products/9")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/stock/products crée un produit")
    void shouldCreateProduct() throws Exception {
        when(stockService.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/stock/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Clavier\",\"quantity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/stock/products retourne 400 si les données sont invalides")
    void shouldReturn400OnInvalidProduct() throws Exception {
        when(stockService.save(any(Product.class))).thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(post("/api/stock/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"quantity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad"));
    }

    @Test
    @DisplayName("DELETE /api/stock/products/{id} retourne 204 puis 404")
    void shouldDelete() throws Exception {
        when(stockService.findById(1L)).thenReturn(Optional.of(product));
        when(stockService.findById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/stock/products/1")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/stock/products/9")).andExpect(status().isNotFound());

        verify(stockService).deleteById(1L);
        verify(stockService, never()).deleteById(9L);
    }

    @Test
    @DisplayName("GET /availability retourne un booléen")
    void shouldCheckAvailability() throws Exception {
        when(stockService.isAvailable(1L, 3)).thenReturn(true);

        mockMvc.perform(get("/api/stock/products/1/availability").param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /reserve décrémente le stock")
    void shouldReserve() throws Exception {
        product.setQuantity(6);
        when(stockService.reserve(1L, 4)).thenReturn(product);

        mockMvc.perform(post("/api/stock/products/1/reserve").param("quantity", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(6));
    }

    @Test
    @DisplayName("POST /reserve retourne 409 si le stock est insuffisant")
    void shouldReturn409WhenInsufficient() throws Exception {
        when(stockService.reserve(1L, 50)).thenThrow(new InsufficientStockException(1L, 50, 10));

        mockMvc.perform(post("/api/stock/products/1/reserve").param("quantity", "50"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /reserve retourne 404 si le produit n'existe pas")
    void shouldReturn404WhenUnknown() throws Exception {
        when(stockService.reserve(9L, 1)).thenThrow(new ProductNotFoundException(9L));

        mockMvc.perform(post("/api/stock/products/9/reserve").param("quantity", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /restock incrémente le stock")
    void shouldRestock() throws Exception {
        product.setQuantity(15);
        when(stockService.restock(1L, 5)).thenReturn(product);

        mockMvc.perform(post("/api/stock/products/1/restock").param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }
}
