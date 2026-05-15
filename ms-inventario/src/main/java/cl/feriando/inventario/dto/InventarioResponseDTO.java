package cl.feriando.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventarioResponseDTO(
        Long idInventario,
        Long idProducto,
        Long idFeriante,
        BigDecimal stockDisponible,
        BigDecimal stockMinimo,
        Short alertaActiva,
        LocalDateTime ultimaActualizacion
) { }
