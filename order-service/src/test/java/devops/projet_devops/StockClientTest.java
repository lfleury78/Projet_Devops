package devops.projet_devops;

import devops.projet_devops.client.StockClient;
import devops.projet_devops.exception.OutOfStockException;
import devops.projet_devops.exception.StockUnavailableException;
import devops.projet_devops.exception.UnknownProductException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StockClientTest {

    private static final String BASE = "http://stock.test";

    private MockRestServiceServer server;
    private StockClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new StockClient(builder, BASE);
    }

    @Test
    @DisplayName("reserve() appelle POST /reserve avec la quantité")
    void shouldCallReserve() {
        server.expect(requestTo(BASE + "/api/stock/products/7/reserve?quantity=4"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        assertThatCode(() -> client.reserve(7L, 4)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    @DisplayName("release() appelle POST /restock avec la quantité")
    void shouldCallRestock() {
        server.expect(requestTo(BASE + "/api/stock/products/7/restock?quantity=4"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        client.release(7L, 4);
        server.verify();
    }

    @Test
    @DisplayName("409 → OutOfStockException")
    void shouldMapConflict() {
        server.expect(requestTo(BASE + "/api/stock/products/7/reserve?quantity=4"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> client.reserve(7L, 4)).isInstanceOf(OutOfStockException.class);
    }

    @Test
    @DisplayName("404 → UnknownProductException")
    void shouldMapNotFound() {
        server.expect(requestTo(BASE + "/api/stock/products/7/reserve?quantity=4"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.reserve(7L, 4)).isInstanceOf(UnknownProductException.class);
    }

    @Test
    @DisplayName("5xx → StockUnavailableException")
    void shouldMapServerError() {
        server.expect(requestTo(BASE + "/api/stock/products/7/reserve?quantity=4"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.reserve(7L, 4)).isInstanceOf(StockUnavailableException.class);
    }
}
