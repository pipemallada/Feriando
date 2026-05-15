package cl.feriando.carrito.dto;

import java.math.BigDecimal;

public record DetalleCarritoResponseDTO(
        Long idDetalle,
        Long idProducto,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) { }
