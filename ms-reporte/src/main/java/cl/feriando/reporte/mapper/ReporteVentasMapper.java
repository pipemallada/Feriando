package cl.feriando.reporte.mapper;

import cl.feriando.reporte.dto.ReporteVentasRequestDTO;
import cl.feriando.reporte.dto.ReporteVentasResponseDTO;
import cl.feriando.reporte.model.ReporteVentas;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReporteVentasMapper {

    public ReporteVentas toEntity(ReporteVentasRequestDTO dto) {
        ReporteVentas r = new ReporteVentas();
        r.setIdFeriante(dto.idFeriante());
        r.setPeriodo(dto.periodo());
        r.setTotalVentas(dto.totalVentas());
        r.setTotalPedidos(dto.totalPedidos());
        r.setProductoTop(dto.productoTop());
        r.setGeneradoEn(LocalDateTime.now());
        return r;
    }

    public ReporteVentasResponseDTO toResponse(ReporteVentas r) {
        return new ReporteVentasResponseDTO(
                r.getIdReporte(),
                r.getIdFeriante(),
                r.getPeriodo(),
                r.getTotalVentas(),
                r.getTotalPedidos(),
                r.getProductoTop(),
                r.getGeneradoEn()
        );
    }
}
