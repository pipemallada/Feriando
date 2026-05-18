package cl.feriando.feriante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * punto de entrada de ms-feriante.
 * es el dueño del perfil del feriante (puesto, descripcion, telefono,
 *   calificacion promedio). vive separado de ms-usuario porque "ser usuario"
 *   y "ser feriante con un puesto" son conceptos distintos.
 *  habla con ms-usuario por WebClient para validar que el id_usuario sea
 *   real antes de crear el perfil.
 */
@SpringBootApplication
public class MsFerianteApplication {
    // arranca el contenedor en el puerto 8082.
    public static void main(String[] args) {
        SpringApplication.run(MsFerianteApplication.class, args);
    }
}
