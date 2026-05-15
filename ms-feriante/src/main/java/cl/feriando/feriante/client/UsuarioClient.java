package cl.feriando.feriante.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public UsuarioClient(WebClient usuarioWebClient) {
        this.client = usuarioWebClient;
    }

    public boolean existeUsuario(Long idUsuario) {
        try {
            HttpStatusCode status = client.get()
                    .uri("/usuarios/{id}", idUsuario)
                    .retrieve()
                    .toBodilessEntity()
                    .map(r -> r.getStatusCode())
                    .onErrorReturn(org.springframework.http.HttpStatus.NOT_FOUND)
                    .block(TIMEOUT);
            return status != null && status.is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("No se pudo validar usuario {} contra ms-usuario: {}", idUsuario, ex.getMessage());
            return false;
        }
    }
}
