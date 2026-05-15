package cl.feriando.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PedidoRequestDTO(

        @NotNull(message = "El id_cliente es obligatorio")
        @Positive
        Long idCliente,

        @NotNull(message = "El id_feriante es obligatorio")
        @Positive
        Long idFeriante,

        @NotEmpty(message = "El pedido debe tener al menos un detalle")
        @Valid
        List<DetallePedidoRequestDTO> detalles
) { }
