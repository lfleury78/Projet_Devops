package devops.projet_devops.controller;

import devops.projet_devops.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/total")
    public BigDecimal calculateTotal(@RequestParam List<BigDecimal> prices) {
        return orderService.calculateTotal(prices);
    }

    @GetMapping("/discount")
    public BigDecimal applyDiscount(
            @RequestParam BigDecimal total,
            @RequestParam int discountPercent
    ) {
        return orderService.applyDiscount(total, discountPercent);
    }

    @GetMapping("/delivery-days")
    public int estimateDeliveryDays(
            @RequestParam boolean priority,
            @RequestParam int itemCount
    ) {
        return orderService.estimateDeliveryDays(priority, itemCount);
    }
}
