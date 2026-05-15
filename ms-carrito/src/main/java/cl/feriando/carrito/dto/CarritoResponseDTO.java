package cl.feriando.carrito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CarritoResponseDTO(
        Long idCarrito,
        Long idCliente,
        String estado,
        LocalDateTime createdAt,
        List<DetalleCarritoResponseDTO> detalles,
        BigDecimal total
) { }
