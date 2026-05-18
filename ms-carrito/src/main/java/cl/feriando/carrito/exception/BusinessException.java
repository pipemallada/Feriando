package cl.feriando.carrito.exception;
/**
 * reglas de negocio violadas: ej. agregar item a un carrito cerrado o un
 * producto inexistente: -> HTTP 400.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
