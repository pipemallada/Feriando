package cl.feriando.calificacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * configura el WebClient hacia ms-feriante (para actualizar el promedio
 * cuando se crea/elimina una calificacion).
 */
@Configuration
public class WebClientConfig {
    // URL parametrizada en application.properties para poder cambiarla sin recompilar.
    @Bean
    public WebClient ferianteWebClient(@Value("${feriando.ms-feriante.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }
}
