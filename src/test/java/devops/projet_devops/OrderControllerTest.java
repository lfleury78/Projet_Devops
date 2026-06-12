package devops.projet_devops;

import tools.jackson.databind.ObjectMapper;
import devops.projet_devops.controller.OrderController;
import devops.projet_devops.model.Order;
import devops.projet_devops.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order(1L, new BigDecimal("100.00"), 10, false, 3, 3);
        sampleOrder.setId(1L);
    }

    @Nested
    @DisplayName("GET /api/orders")
    class FindAll {

        @Test
        @DisplayName("retourne la liste de toutes les commandes")
        void shouldReturnAllOrders() throws Exception {
            when(orderService.findAll()).thenReturn(List.of(sampleOrder));

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].userId").value(1))
                    .andExpect(jsonPath("$[0].total").value(100.00))
                    .andExpect(jsonPath("$[0].discountPercent").value(10))
                    .andExpect(jsonPath("$[0].priority").value(false))
                    .andExpect(jsonPath("$[0].itemCount").value(3))
                    .andExpect(jsonPath("$[0].deliveryDays").value(3));

            verify(orderService, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class FindById {

        @Test
        @DisplayName("retourne 200 et la commande quand elle existe")
        void shouldReturnOrderWhenFound() throws Exception {
            when(orderService.findById(1L)).thenReturn(Optional.of(sampleOrder));

            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.total").value(100.00));
        }

        @Test
        @DisplayName("retourne 404 quand la commande n'existe pas")
        void shouldReturnNotFoundWhenMissing() throws Exception {
            when(orderService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/orders/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/user/{userId}")
    class FindByUserId {

        @Test
        @DisplayName("retourne les commandes d'un utilisateur")
        void shouldReturnOrdersByUser() throws Exception {
            when(orderService.findByUserId(1L)).thenReturn(List.of(sampleOrder));

            mockMvc.perform(get("/api/orders/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].userId").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/orders")
    class Create {

        @Test
        @DisplayName("retourne 201 et la commande créée")
        void shouldCreateOrder() throws Exception {
            Order newOrder = new Order(1L, new BigDecimal("50.00"), 0, false, 2, 0);
            Order savedOrder = new Order(1L, new BigDecimal("50.00"), 0, false, 2, 3);
            savedOrder.setId(2L);

            when(orderService.save(any(Order.class))).thenReturn(savedOrder);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newOrder)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.total").value(50.00));

            verify(orderService, times(1)).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/orders/{id}")
    class Update {

        @Test
        @DisplayName("retourne 200 et la commande mise à jour")
        void shouldUpdateOrder() throws Exception {
            Order updatedOrder = new Order(1L, new BigDecimal("120.00"), 5, true, 4, 1);
            updatedOrder.setId(1L);

            when(orderService.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderService.save(any(Order.class))).thenReturn(updatedOrder);

            mockMvc.perform(put("/api/orders/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedOrder)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(120.00))
                    .andExpect(jsonPath("$.priority").value(true));
        }

        @Test
        @DisplayName("retourne 404 quand la commande n'existe pas")
        void shouldReturnNotFoundWhenMissing() throws Exception {
            when(orderService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/orders/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleOrder)))
                    .andExpect(status().isNotFound());

            verify(orderService, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/orders/{id}")
    class Delete {

        @Test
        @DisplayName("retourne 204 quand la commande est supprimée")
        void shouldDeleteOrder() throws Exception {
            when(orderService.findById(1L)).thenReturn(Optional.of(sampleOrder));

            mockMvc.perform(delete("/api/orders/1"))
                    .andExpect(status().isNoContent());

            verify(orderService, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("retourne 404 quand la commande n'existe pas")
        void shouldReturnNotFoundWhenMissing() throws Exception {
            when(orderService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/orders/99"))
                    .andExpect(status().isNotFound());

            verify(orderService, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/total")
    class CalculateTotal {

        @Test
        @DisplayName("retourne le total calculé")
        void shouldReturnCalculatedTotal() throws Exception {
            when(orderService.calculateTotal(List.of(
                    new BigDecimal("10.50"),
                    new BigDecimal("20.00"),
                    new BigDecimal("5.75")
            ))).thenReturn(new BigDecimal("36.25"));

            mockMvc.perform(get("/api/orders/total")
                            .param("prices", "10.50", "20.00", "5.75"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("36.25"));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/discount")
    class ApplyDiscount {

        @Test
        @DisplayName("retourne le total après remise")
        void shouldReturnDiscountedTotal() throws Exception {
            when(orderService.applyDiscount(new BigDecimal("100.00"), 10))
                    .thenReturn(new BigDecimal("90.00"));

            mockMvc.perform(get("/api/orders/discount")
                            .param("total", "100.00")
                            .param("discountPercent", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("90.00"));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/delivery-days")
    class EstimateDeliveryDays {

        @Test
        @DisplayName("retourne le nombre de jours de livraison estimé")
        void shouldReturnEstimatedDays() throws Exception {
            when(orderService.estimateDeliveryDays(true, 3)).thenReturn(1);

            mockMvc.perform(get("/api/orders/delivery-days")
                            .param("priority", "true")
                            .param("itemCount", "3"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1"));
        }
    }
}