package cl.feriando.carrito.client;

import cl.feriando.carrito.dto.ProductoBasicoDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Component
public class ProductoClient {

    private static final Logger log = LoggerFactory.getLogger(ProductoClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient client;

    public ProductoClient(WebClient productoWebClient) {
        this.client = productoWebClient;
    }

    public Optional<ProductoBasicoDTO> buscarProducto(Long idProducto) {
        try {
            ProductoBasicoDTO p = client.get()
                    .uri("/productos/{id}", idProducto)
                    .retrieve()
                    .bodyToMono(ProductoBasicoDTO.class)
                    .block(TIMEOUT);
            return Optional.ofNullable(p);
        } catch (Exception ex) {
            log.warn("No se pudo consultar producto {} en ms-producto: {}", idProducto, ex.getMessage());
            return Optional.empty();
        }
    }
}
