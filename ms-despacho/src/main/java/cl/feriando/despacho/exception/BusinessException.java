package cl.feriando.despacho.exception;
/**
 * reglas de negocio: pedido con despacho previo, direccion faltante para
 * DOMICILIO, etc. -> HTTP 400.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
