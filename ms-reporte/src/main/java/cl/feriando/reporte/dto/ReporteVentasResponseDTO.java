package cl.feriando.reporte.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteVentasResponseDTO(
        Long idReporte,
        Long idFeriante,
        String periodo,
        BigDecimal totalVentas,
        Integer totalPedidos,
        String productoTop,
        LocalDateTime generadoEn
) { }
