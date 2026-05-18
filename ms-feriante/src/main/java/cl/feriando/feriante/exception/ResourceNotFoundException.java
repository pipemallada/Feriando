package cl.feriando.feriante.exception;
/**
 * excepcion para "no se encontro el feriante con ese id".
 * el GlobalExceptionHandler la traduce a HTTP 404 con JSON uniforme.
 * misma estrategia que en ms-usuario.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
