package cl.feriando.carrito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/**
 * DTO de salida del carrito completo.
 * el total es calculado (suma de subtotales). igual que con subtotal en
 * el detalle, lo enviamos al cliente para que no tenga que recalcular.
 * si el total viene persistido en la entidad, podría desincronizarse de
 * sus detalles; al calcularlo siempre, es imposible.
 */
public record CarritoResponseDTO(
        Long idCarrito,
        Long idCliente,
        String estado,
        LocalDateTime createdAt,
        List<DetalleCarritoResponseDTO> detalles,
        BigDecimal total
) { }
