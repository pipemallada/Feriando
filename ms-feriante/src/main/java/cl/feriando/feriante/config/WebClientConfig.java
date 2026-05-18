package cl.feriando.feriante.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
/**
 * configuración del WebClient usado para hablar con otros microservicios.
 *  permite reutilizar el cliente (un connection pool, una configuracion).
 * pone la URL base en application.properties; cambiarla no requiere recompilar.
 */
@Configuration
public class WebClientConfig {
    // @Value lee la URL de application.properties. el nombre del bean
    // (usuarioWebClient) lo usa UsuarioClient para inyectar este cliente.
    @Bean
    public WebClient usuarioWebClient(@Value("${feriando.ms-usuario.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }
}
