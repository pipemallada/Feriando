package cl.feriando.calificacion.exception;
/**
 * calificacion no encontrada -> HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
