package cl.feriando.usuario.exception;
/**
 * excepcion para violaciones de reglas de negocio.
 * esta se traduce a HTTP 400 (Bad Request). el cliente envió algo válido,
 * pero que rompe una regla.
 * 404 vs 400 transmiten informacion distinta al consumidor del API.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
