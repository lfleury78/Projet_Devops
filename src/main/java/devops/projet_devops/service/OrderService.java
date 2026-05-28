package devops.projet_devops.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

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
