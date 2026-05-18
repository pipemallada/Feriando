package cl.feriando.feriante.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
/**
 * cliente HTTP para hablar con ms-usuario.
 * encapsula los detalles del HTTP (timeout, manejo de errores, deserializacion).
 * el service queda con un metodo claro (ExisteUsuario) en vez de un fluent
 *   API de WebClient que mezcla responsabilidades.
 * si mañana cambiamos a Feign o gRPC, solo se cambia este archivo.
 */
@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);
    // timeout corto: si ms-usuario no responde en 3s consideramos que esta caido.
    // evita que ms-feriante quede colgado esperando indefinidamente.
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;
    // inyeccion por nombre del bean: "usuarioWebClient" lo registra WebClientConfig.
    public UsuarioClient(WebClient usuarioWebClient) {
        this.client = usuarioWebClient;
    }
    /**
     * devuelve true solo si llegamos a 2xx. en cualquier otro caso (404,
     * timeout, conexion rechazada) devuelve false y registra un warning, NO
     * propaga la excepcion: la decision la toma el service.
     */
    public boolean existeUsuario(Long idUsuario) {
        try {
            HttpStatusCode status = client.get()
                    .uri("/usuarios/{id}", idUsuario)
                    // toBodilessEntity: no necesitamos parsear el body, solo el status.
                    .retrieve()
                    .toBodilessEntity()
                    .map(r -> r.getStatusCode())
                    // si Spring lanza WebClientResponseException por 4xx/5xx,
                    // lo convertimos en NOT_FOUND para no romper el flujo.
                    .onErrorReturn(org.springframework.http.HttpStatus.NOT_FOUND)
                    // block() con timeout porque el service es bloqueante.
                    .block(TIMEOUT);
            return status != null && status.is2xxSuccessful();
        } catch (Exception ex) {
            // si ms-usuario esta caido o hay problemas de red, lo logueamos
            // y devolvemos false. esto no deberia tirar 500 al cliente final.
            log.warn("No se pudo validar usuario {} contra ms-usuario: {}", idUsuario, ex.getMessage());
            return false;
        }
    }
}
