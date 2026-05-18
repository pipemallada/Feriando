package cl.feriando.carrito.client;

import cl.feriando.carrito.dto.ProductoBasicoDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;
/**
 * cliente HTTP hacia ms-producto.
 * su única responsabilidad es consultar un producto por id para obtener su
 * precio actual.
 * devolvemos Optional<> porque ms-producto puede estar caído o el product
 * no existir: el service decide qué hacer en ese caso.
 */
@Component
public class ProductoClient {

    private static final Logger log = LoggerFactory.getLogger(ProductoClient.class);
    // timeout corto para no colgar al cliente final si ms-producto no responde.
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public ProductoClient(WebClient productoWebClient) {
        this.client = productoWebClient;
    }
    /**
     * consulta un producto por id. Optional.empty() si no existe, si el MS
     * esta caido, o si la llamada tarda más que el timeout.
     */
    public Optional<ProductoBasicoDTO> buscarProducto(Long idProducto) {
        try {
            ProductoBasicoDTO p = client.get()
                    .uri("/productos/{id}", idProducto)
                    .retrieve()
                    // bodyToMono + block: lo convertimos a llamada bloqueante
                    // porque el service de carrito es bloqueante.
                    .bodyToMono(ProductoBasicoDTO.class)
                    .block(TIMEOUT);
            return Optional.ofNullable(p);
        } catch (Exception ex) {
            // log a nivel WARN: es esperable que esto pase si ms-producto
            // esta caido o el producto no existe (404).
            log.warn("No se pudo consultar producto {} en ms-producto: {}", idProducto, ex.getMessage());
            return Optional.empty();
        }
    }
}
