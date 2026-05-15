package cl.feriando.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AjusteStockDTO(

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser positiva")
        BigDecimal cantidad
) { }
