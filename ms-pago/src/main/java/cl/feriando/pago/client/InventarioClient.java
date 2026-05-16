package cl.feriando.pago.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Component
public class InventarioClient {

    private static final Logger log = LoggerFactory.getLogger(InventarioClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public InventarioClient(WebClient inventarioWebClient) {
        this.client = inventarioWebClient;
    }

    public boolean descontarStock(Long idProducto, BigDecimal cantidad) {
        try {
            client.patch()
                    .uri("/inventario/producto/{idProducto}/descontar", idProducto)
                    .bodyValue(Map.of("cantidad", cantidad))
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.info("Stock descontado en ms-inventario producto={}, cantidad={}", idProducto, cantidad);
            return true;
        } catch (Exception ex) {
            log.warn("No se pudo descontar stock del producto {}: {}", idProducto, ex.getMessage());
            return false;
        }
    }
}
