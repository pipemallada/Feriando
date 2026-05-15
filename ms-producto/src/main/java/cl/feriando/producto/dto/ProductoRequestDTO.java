package cl.feriando.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequestDTO(

        @NotNull(message = "El id_feriante es obligatorio")
        @Positive
        Long idFeriante,

        @NotNull(message = "El id_categoria es obligatorio")
        @Positive
        Long idCategoria,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @Size(max = 1000)
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        BigDecimal precio,

        @Size(max = 10)
        String unidad
) { }
