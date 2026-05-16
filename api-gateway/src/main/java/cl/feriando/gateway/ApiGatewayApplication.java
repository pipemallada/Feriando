package cl.feriando.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del API Gateway.
 * - Centraliza TODAS las peticiones del cliente en un solo host:puerto (8080).
 * - Evita que el frontend tenga que conocer los 10 puertos internos.
 * No tiene base de datos ni lógica de negocio: sólo enruta.
 */

@SpringBootApplication
public class ApiGatewayApplication {

    // Metodo main estandar de Spring Boot.
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
