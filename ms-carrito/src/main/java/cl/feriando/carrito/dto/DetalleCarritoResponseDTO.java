package cl.feriando.carrito.dto;

import java.math.BigDecimal;
/**
 * DTO de salida de cada item del carrito.
 * subtotal es un campo calculado (cantidad × precioUnitario). no esta en
 * la entidad. lo calculamos en el mapper para que el frontend no tenga que
 * hacer la multiplicacion por su cuenta.
 */
public record DetalleCarritoResponseDTO(
        Long idDetalle,
        Long idProducto,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) { }
