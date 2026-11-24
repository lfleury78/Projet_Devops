package devops.projet_devops.client;

import devops.projet_devops.exception.OutOfStockException;
import devops.projet_devops.exception.StockUnavailableException;
import devops.projet_devops.exception.UnknownProductException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Client HTTP du stock-service. */
@Component
public class StockClient {

    private final RestClient restClient;

    @Autowired
    public StockClient(@Value("${stock.service.url:http://localhost:8081}") String baseUrl) {
        this(RestClient.builder(), baseUrl);
    }

    /** Permet d'injecter un builder préconfiguré (utilisé par les tests). */
    public StockClient(RestClient.Builder builder, String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /** Décrémente le stock du produit. */
    public void reserve(Long productId, int quantity) {
        call("reserve", productId, quantity);
    }

    /** Remet en stock (compensation d'une réservation). */
    public void release(Long productId, int quantity) {
        call("restock", productId, quantity);
    }

    private void call(String action, Long productId, int quantity) {
        try {
            restClient.post()
                    .uri("/api/stock/products/{id}/{action}?quantity={q}", productId, action, quantity)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                throw new UnknownProductException(productId);
            }
            if (e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
                throw new OutOfStockException("Insufficient stock for product " + productId);
            }
            throw new StockUnavailableException("stock-service error: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            throw new StockUnavailableException("stock-service unreachable", e);
        }
    }
}
