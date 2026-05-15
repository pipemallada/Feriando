package cl.feriando.carrito.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CarritoRequestDTO(

        @NotNull(message = "El id_cliente es obligatorio")
        @Positive
        Long idCliente
) { }
