package cl.feriando.reporte.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoResumenDTO(
        Long idPedido,
        Long idFeriante,
        String estado,
        BigDecimal total,
        LocalDateTime fechaPedido,
        List<DetalleResumenDTO> detalles
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetalleResumenDTO(
            Long idProducto,
            BigDecimal cantidad
    ) { }
}
