package cl.feriando.despacho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * DTO para PATCH /despachos/{id}/estado.
 restringe los valores validos a los estados que reconoce la logica del
 despacho. cualquier otro String se rechaza con 400 antes del service.
 */
public record CambioEstadoDespachoDTO(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "PENDIENTE|EN_RUTA|ENTREGADO|CANCELADO",
                message = "Estado inválido. Valores: PENDIENTE, EN_RUTA, ENTREGADO, CANCELADO"
        )
        String estadoDespacho
) { }
