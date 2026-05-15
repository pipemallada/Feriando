package cl.feriando.carrito.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productoWebClient(@Value("${feriando.ms-producto.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }
}
