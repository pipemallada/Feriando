package cl.feriando.calificacion.exception;
/**
 * reglas de negocio (ejemplo calificacion duplicada por pedido) -> HTTP 400.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
