package cl.feriando.feriante.exception;
/**
 * excepcion para violaciones de reglas de negocio (ej. usuario ya tiene
 * feriante, o el usuario no existe en ms-usuario).
 * se traduce a HTTP 400 Bad Request.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
