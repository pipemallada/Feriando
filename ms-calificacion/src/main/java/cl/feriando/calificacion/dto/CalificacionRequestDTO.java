package cl.feriando.calificacion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CalificacionRequestDTO(

        @NotNull(message = "El id_pedido es obligatorio")
        @Positive
        Long idPedido,

        @NotNull(message = "El id_cliente es obligatorio")
        @Positive
        Long idCliente,

        @NotNull(message = "El id_feriante es obligatorio")
        @Positive
        Long idFeriante,

        @NotNull(message = "El puntaje es obligatorio")
        @Min(value = 1, message = "El puntaje mínimo es 1")
        @Max(value = 5, message = "El puntaje máximo es 5")
        Short puntaje,

        @Size(max = 1000)
        String comentario
) { }
