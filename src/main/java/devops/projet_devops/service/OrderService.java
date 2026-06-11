package devops.projet_devops.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import devops.projet_devops.model.Order;
import devops.projet_devops.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order save(Order order) {
        BigDecimal discountedTotal = applyDiscount(order.getTotal(), order.getDiscountPercent());
        order.setTotal(discountedTotal);
        order.setDeliveryDays(estimateDeliveryDays(order.isPriority(), order.getItemCount()));
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    public BigDecimal calculateTotal(List<BigDecimal> prices) {
        if (prices == null || prices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal applyDiscount(BigDecimal total, int discountPercent) {
        if (total == null) {
            throw new IllegalArgumentException("total cannot be null");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("discountPercent must be between 0 and 100");
        }

        BigDecimal discount = total
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return total.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    public int estimateDeliveryDays(boolean priority, int itemCount) {
        if (itemCount < 0) {
            throw new IllegalArgumentException("itemCount cannot be negative");
        }
        if (priority) {
            return itemCount > 5 ? 2 : 1;
        }
        return itemCount > 5 ? 5 : 3;
    }
}
