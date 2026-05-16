package cl.feriando.despacho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record DespachoRequestDTO(

        @NotNull(message = "El id_pedido es obligatorio")
        @Positive
        Long idPedido,

        @NotBlank(message = "El tipo de entrega es obligatorio")
        @Pattern(regexp = "RETIRO|DOMICILIO", message = "Debe ser RETIRO o DOMICILIO")
        String tipoEntrega,

        @Size(max = 255)
        String direccion,

        @Size(max = 100)
        String comuna,

        LocalDateTime fechaEstimada
) { }
