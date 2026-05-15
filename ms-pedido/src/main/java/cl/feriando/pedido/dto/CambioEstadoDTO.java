package cl.feriando.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioEstadoDTO(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "PENDIENTE|PAGADO|PREPARANDO|LISTO|ENTREGADO|CANCELADO",
                message = "Estado inválido. Valores: PENDIENTE, PAGADO, PREPARANDO, LISTO, ENTREGADO, CANCELADO"
        )
        String estado
) { }
