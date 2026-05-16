package cl.feriando.despacho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioEstadoDespachoDTO(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "PENDIENTE|EN_RUTA|ENTREGADO|CANCELADO",
                message = "Estado inválido. Valores: PENDIENTE, EN_RUTA, ENTREGADO, CANCELADO"
        )
        String estadoDespacho
) { }
