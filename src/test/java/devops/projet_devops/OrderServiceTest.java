package devops.projet_devops;

import devops.projet_devops.service.OrderService;
import devops.projet_devops.model.Order;
import devops.projet_devops.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // total=100, discount=10% → saved total = 90.00, priority=false, items=3 → deliveryDays=3
        sampleOrder = new Order(1L, new BigDecimal("100.00"), 10, false, 3, 0);
        sampleOrder.setId(1L);
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("retourne toutes les commandes")
        void shouldReturnAllOrders() {
            when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

            assertThat(orderService.findAll()).hasSize(1).contains(sampleOrder);
            verify(orderRepository).findAll();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("retourne la commande quand elle existe")
        void shouldReturnOrderWhenFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

            assertThat(orderService.findById(1L)).isPresent().contains(sampleOrder);
        }

        @Test
        @DisplayName("retourne Optional.empty() quand la commande n'existe pas")
        void shouldReturnEmptyWhenNotFound() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(orderService.findById(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("retourne les commandes d'un utilisateur")
        void shouldReturnOrdersByUser() {
            when(orderRepository.findByUserId(1L)).thenReturn(List.of(sampleOrder));

            assertThat(orderService.findByUserId(1L)).hasSize(1).contains(sampleOrder);
        }
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("applique la remise et les jours de livraison avant de sauvegarder")
        void shouldApplyDiscountAndDeliveryBeforeSave() {
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order saved = orderService.save(sampleOrder);

            // 100 - 10% = 90.00
            assertThat(saved.getTotal()).isEqualByComparingTo("90.00");
            // non-priority, items=3 ≤ 5 → 3 days
            assertThat(saved.getDeliveryDays()).isEqualTo(3);
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("commande prioritaire avec > 5 articles → 2 jours")
        void shouldSet2DaysForPriorityHighItemCount() {
            Order priority = new Order(1L, new BigDecimal("50.00"), 0, true, 6, 0);
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Order saved = orderService.save(priority);

            assertThat(saved.getDeliveryDays()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("appelle deleteById sur le repository")
        void shouldCallRepositoryDelete() {
            orderService.deleteById(1L);

            verify(orderRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("calculateTotal()")
    class CalculateTotal {

        @Test
        @DisplayName("calcule la somme de plusieurs prix")
        void shouldSumPrices() {
            List<BigDecimal> prices = List.of(
                    new BigDecimal("10.50"),
                    new BigDecimal("20.00"),
                    new BigDecimal("5.75")
            );

            BigDecimal result = orderService.calculateTotal(prices);

            assertThat(result).isEqualByComparingTo("36.25");
        }

        @Test
        @DisplayName("retourne 0 pour une liste vide")
        void shouldReturnZeroForEmptyList() {
            assertThat(orderService.calculateTotal(List.of()))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("retourne 0 pour null")
        void shouldReturnZeroForNull() {
            assertThat(orderService.calculateTotal(null))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("arrondit à 2 décimales")
        void shouldRoundToTwoDecimals() {
            List<BigDecimal> prices = List.of(
                    new BigDecimal("0.001"),
                    new BigDecimal("0.005")
            );

            BigDecimal result = orderService.calculateTotal(prices);

            assertThat(result.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("applyDiscount()")
    class ApplyDiscount {

        @Test
        @DisplayName("applique une remise de 10% correctement")
        void shouldApply10PercentDiscount() {
            BigDecimal result = orderService.applyDiscount(new BigDecimal("100.00"), 10);

            assertThat(result).isEqualByComparingTo("90.00");
        }

        @Test
        @DisplayName("remise 0% retourne le total inchangé")
        void shouldReturnSameTotalForZeroDiscount() {
            BigDecimal result = orderService.applyDiscount(new BigDecimal("50.00"), 0);

            assertThat(result).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("remise 100% retourne 0")
        void shouldReturnZeroForFullDiscount() {
            BigDecimal result = orderService.applyDiscount(new BigDecimal("200.00"), 100);

            assertThat(result).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("lève une exception pour un total null")
        void shouldThrowForNullTotal() {
            assertThatThrownBy(() -> orderService.applyDiscount(null, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("total cannot be null");
        }

        @Test
        @DisplayName("lève une exception pour une remise négative")
        void shouldThrowForNegativeDiscount() {
            assertThatThrownBy(() -> orderService.applyDiscount(new BigDecimal("100"), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lève une exception pour une remise > 100")
        void shouldThrowForDiscountOver100() {
            assertThatThrownBy(() -> orderService.applyDiscount(new BigDecimal("100"), 101))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("estimateDeliveryDays()")
    class EstimateDeliveryDays {

        @Test
        @DisplayName("prioritaire avec <= 5 articles → 1 jour")
        void shouldReturn1DayForPriorityFewItems() {
            assertThat(orderService.estimateDeliveryDays(true, 3)).isEqualTo(1);
        }

        @Test
        @DisplayName("prioritaire avec > 5 articles → 2 jours")
        void shouldReturn2DaysForPriorityManyItems() {
            assertThat(orderService.estimateDeliveryDays(true, 6)).isEqualTo(2);
        }

        @Test
        @DisplayName("non-prioritaire avec <= 5 articles → 3 jours")
        void shouldReturn3DaysForNonPriorityFewItems() {
            assertThat(orderService.estimateDeliveryDays(false, 4)).isEqualTo(3);
        }

        @Test
        @DisplayName("non-prioritaire avec > 5 articles → 5 jours")
        void shouldReturn5DaysForNonPriorityManyItems() {
            assertThat(orderService.estimateDeliveryDays(false, 10)).isEqualTo(5);
        }

        @Test
        @DisplayName("lève une exception pour un nombre d'articles négatif")
        void shouldThrowForNegativeItemCount() {
            assertThatThrownBy(() -> orderService.estimateDeliveryDays(false, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemCount cannot be negative");
        }
    }
}