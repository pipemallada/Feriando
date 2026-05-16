package cl.feriando.pago.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDTO(
        Long idPago,
        Long idPedido,
        Long idCliente,
        BigDecimal monto,
        String metodoPago,
        String estadoPago,
        LocalDateTime fechaPago,
        String codigoTransaccion
) { }
