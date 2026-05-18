package cl.feriando.carrito.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
/**
 * configura el WebClient hacia ms-producto.
 * lo dejamos como bean (no como instancia local) para reutilizarlo entre
 * llamadas. WebClient mantiene un connection pool interno que se aprovecha
 * mejor si hay un solo bean compartido.
 */
@Configuration
public class WebClientConfig {
    // la URL del MS viene de application.properties (feriando.ms-producto.url).
    // cambiarla no requiere recompilar.
    @Bean
    public WebClient productoWebClient(@Value("${feriando.ms-producto.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }
}
