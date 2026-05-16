package cl.feriando.calificacion.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * Cliente HTTP hacia ms-feriante.
 *
 * unico metodo: PATCH para empujar
 * el nuevo promedio (calculado aqui) a ms-feriante. Devuelve boolean para
 * que el service decida si tratar el fallo como critico o no.
 */

@Component
public class FerianteClient {

    private static final Logger log = LoggerFactory.getLogger(FerianteClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public FerianteClient(WebClient ferianteWebClient) {
        this.client = ferianteWebClient;
    }
    /**
     * empuja el nuevo promedio al ms-feriante. si falla, log y false
     * (no propagamos: la calificacion local ya esta creada).
     */
    public boolean actualizarPromedio(Long idFeriante, BigDecimal nuevoPromedio) {
        try {
            client.patch()
                    .uri("/feriantes/{id}/calificacion-promedio", idFeriante)
                    // Map.of() para no crear un DTO solo para 1 campo.
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
