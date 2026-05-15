package cl.feriando.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long idPedido,
        Long idCliente,
        Long idFeriante,
        String estado,
        BigDecimal total,
        LocalDateTime fechaPedido,
        List<DetallePedidoResponseDTO> detalles
) { }
