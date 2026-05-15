package cl.feriando.producto.dto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long idProducto,
        Long idFeriante,
        Long idCategoria,
        String nombreCategoria,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String unidad,
        Short activo
) { }
