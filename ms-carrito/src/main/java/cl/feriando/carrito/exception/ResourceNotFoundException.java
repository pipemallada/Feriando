package cl.feriando.carrito.exception;
/**
 * carrito o detalle no encontrado -> HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
