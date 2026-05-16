package cl.feriando.calificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * punto de entrada de ms-calificacion (puerto 8089).
 *
 * registra el puntaje (1 a 5) y comentario del cliente sobre un feriante
 * despues de recibir un pedido. al crear o borrar una calificacion,
 * recalcula el promedio del feriante y lo actualiza en ms-feriante via
 * WebClient.
 */
@SpringBootApplication
public class MsCalificacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCalificacionApplication.class, args);
    }
}
