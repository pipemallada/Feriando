package cl.feriando.calificacion.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Component
public class FerianteClient {

    private static final Logger log = LoggerFactory.getLogger(FerianteClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public FerianteClient(WebClient ferianteWebClient) {
        this.client = ferianteWebClient;
    }

    public boolean actualizarPromedio(Long idFeriante, BigDecimal nuevoPromedio) {
        try {
            client.patch()
                    .uri("/feriantes/{id}/calificacion-promedio", idFeriante)
                    .bodyValue(Map.of("promedio", nuevoPromedio))
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.info("Promedio actualizado en ms-feriante id={}, valor={}", idFeriante, nuevoPromedio);
            return true;
        } catch (Exception ex) {
            log.warn("No se pudo actualizar promedio del feriante {}: {}", idFeriante, ex.getMessage());
            return false;
        }
    }
}
