package cl.feriando.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InventarioRequestDTO(

        @NotNull(message = "El id_producto es obligatorio")
        @Positive
        Long idProducto,

        @NotNull(message = "El id_feriante es obligatorio")
        @Positive
        Long idFeriante,

        @NotNull(message = "El stock disponible es obligatorio")
        @DecimalMin(value = "0.0", message = "El stock no puede ser negativo")
        BigDecimal stockDisponible,

        @NotNull(message = "El stock mínimo es obligatorio")
        @DecimalMin(value = "0.0", message = "El stock mínimo no puede ser negativo")
        BigDecimal stockMinimo
) { }
