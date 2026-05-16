package cl.feriando.pago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagoRequestDTO(

        @NotNull(message = "El id_pedido es obligatorio")
        @Positive
        Long idPedido,

        @NotNull(message = "El id_cliente es obligatorio")
        @Positive
        Long idCliente,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal monto,

        @NotBlank(message = "El método de pago es obligatorio")
        @Pattern(regexp = "EFECTIVO|TARJETA|TRANSFERENCIA",
                message = "Método inválido. Valores: EFECTIVO, TARJETA, TRANSFERENCIA")
        String metodoPago,

        @Size(max = 100)
        String codigoTransaccion
) { }
