package cl.feriando.pago.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoDTO(
        Long idPedido,
        Long idCliente,
        Long idFeriante,
        String estado,
        BigDecimal total,
        List<DetallePedidoDTO> detalles
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetallePedidoDTO(
            Long idProducto,
            BigDecimal cantidad
    ) { }
}
