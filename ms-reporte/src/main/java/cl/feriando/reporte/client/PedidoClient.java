package cl.feriando.reporte.client;

import cl.feriando.reporte.dto.PedidoResumenDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class PedidoClient {

    private static final Logger log = LoggerFactory.getLogger(PedidoClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient client;

    public PedidoClient(WebClient pedidoWebClient) {
        this.client = pedidoWebClient;
    }

    public List<PedidoResumenDTO> listarPorFeriante(Long idFeriante) {
        try {
            List<PedidoResumenDTO> pedidos = client.get()
                    .uri(uri -> uri.path("/pedidos").queryParam("idFeriante", idFeriante).build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PedidoResumenDTO>>() { })
                    .block(TIMEOUT);
            return pedidos == null ? Collections.emptyList() : pedidos;
        } catch (Exception ex) {
            log.warn("No se pudieron listar los pedidos del feriante {}: {}", idFeriante, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
