package cl.feriando.despacho.exception;
/**
 * "despacho no encontrado" -> HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
