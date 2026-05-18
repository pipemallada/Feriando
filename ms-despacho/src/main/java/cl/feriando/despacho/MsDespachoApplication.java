package cl.feriando.despacho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * punto de entrada de ms-despacho (puerto 8087).
 * maneja la logistica del pedido: tipo (retiro en feria o domicilio),
 * direccion, comuna y estado de la entrega (pendiente, en_ruta, entregado).
 * es independiente de ms-pedido porque la logica de despacho (asignacion,
 * fechas estimadas) puede crecer mucho sin afectar el ciclo del pedido.
 */
@SpringBootApplication
public class MsDespachoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsDespachoApplication.class, args);
    }
}
