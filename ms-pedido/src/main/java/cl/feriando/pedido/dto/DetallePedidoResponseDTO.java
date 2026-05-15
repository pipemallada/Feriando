package cl.feriando.pedido.dto;

import java.math.BigDecimal;

public record DetallePedidoResponseDTO(
        Long idDetalle,
        Long idProducto,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) { }
