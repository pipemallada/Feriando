package cl.feriando.carrito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * punto de entrada de ms-carrito (puerto 8085).
 *  es una entidad efimera (el cliente la modifica muchas veces antes de
 *   comprar). aislada, no compite por recursos con catalogo o pedidos.
 *  habla con ms-producto via WebClient para validar precios al momento
 *   de agregar un item (no confiar del precio que mande el cliente).
 */
@SpringBootApplication
public class MsCarritoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCarritoApplication.class, args);
    }
}
