package cl.feriando.pedido.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DetallePedidoRequestDTO(

        @NotNull(message = "El id_producto es obligatorio")
        @Positive
        Long idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal precioUnitario
) { }
