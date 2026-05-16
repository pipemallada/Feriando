package cl.feriando.calificacion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
/**
 * DTO de entrada para crear una calificacion.
 *
 * No tenemos updateEntity ni un DTO de update: una vez creada la calificacion
 * no se edita (seria raro permitir que un cliente cambie su nota).
 * si quiere corregirla debe borrar y crear de nuevo.
 */
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

        // @Min y @Max acotan el puntaje al rango válido 1..5.
        // Bean Validation lo rechaza con 400 si viene fuera de rango.
        @NotNull(message = "El puntaje es obligatorio")
        @Min(value = 1, message = "El puntaje mínimo es 1")
        @Max(value = 5, message = "El puntaje máximo es 5")
        Short puntaje,

        @Size(max = 1000)
        String comentario
) { }
