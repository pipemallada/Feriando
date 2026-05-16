package cl.feriando.reporte.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ReporteVentasRequestDTO(

        @NotNull(message = "El id_feriante es obligatorio")
        @Positive
        Long idFeriante,

        @NotBlank(message = "El periodo es obligatorio (ej. 2026-05)")
        @Size(max = 20)
        String periodo,

        @NotNull(message = "El total de ventas es obligatorio")
        @DecimalMin(value = "0.0", message = "El total no puede ser negativo")
        BigDecimal totalVentas,

        @NotNull(message = "El total de pedidos es obligatorio")
        @Min(value = 0, message = "El total de pedidos no puede ser negativo")
        Integer totalPedidos,

        @Size(max = 150)
        String productoTop
) { }
