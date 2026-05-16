package cl.feriando.pago.client;

import cl.feriando.pago.dto.PedidoDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
public class PedidoClient {

    private static final Logger log = LoggerFactory.getLogger(PedidoClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public PedidoClient(WebClient pedidoWebClient) {
        this.client = pedidoWebClient;
    }

    public Optional<PedidoDTO> buscarPedido(Long idPedido) {
        try {
            PedidoDTO pedido = client.get()
                    .uri("/pedidos/{id}", idPedido)
                    .retrieve()
                    .bodyToMono(PedidoDTO.class)
                    .block(TIMEOUT);
            return Optional.ofNullable(pedido);
        } catch (Exception ex) {
            log.warn("No se pudo obtener pedido {}: {}", idPedido, ex.getMessage());
            return Optional.empty();
        }
    }

    public boolean marcarComoPagado(Long idPedido) {
        try {
            client.patch()
                    .uri("/pedidos/{id}/estado", idPedido)
                    .bodyValue(Map.of("estado", "PAGADO"))
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.info("Pedido {} marcado como PAGADO en ms-pedido", idPedido);
            return true;
        } catch (Exception ex) {
            log.warn("No se pudo cambiar estado del pedido {} a PAGADO: {}", idPedido, ex.getMessage());
            return false;
        }
    }
}
