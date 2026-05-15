package cl.feriando.inventario.mapper;

import cl.feriando.inventario.dto.InventarioRequestDTO;
import cl.feriando.inventario.dto.InventarioResponseDTO;
import cl.feriando.inventario.model.Inventario;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InventarioMapper {

    public Inventario toEntity(InventarioRequestDTO dto) {
        Inventario i = new Inventario();
        i.setIdProducto(dto.idProducto());
        i.setIdFeriante(dto.idFeriante());
        i.setStockDisponible(dto.stockDisponible());
        i.setStockMinimo(dto.stockMinimo());
        i.setAlertaActiva(calcularAlerta(dto.stockDisponible(), dto.stockMinimo()));
        i.setUltimaActualizacion(LocalDateTime.now());
        return i;
    }

    public InventarioResponseDTO toResponse(Inventario i) {
        return new InventarioResponseDTO(
                i.getIdInventario(),
                i.getIdProducto(),
                i.getIdFeriante(),
                i.getStockDisponible(),
                i.getStockMinimo(),
                i.getAlertaActiva(),
                i.getUltimaActualizacion()
        );
    }

    public void updateEntity(Inventario i, InventarioRequestDTO dto) {
        i.setIdFeriante(dto.idFeriante());
        i.setStockDisponible(dto.stockDisponible());
        i.setStockMinimo(dto.stockMinimo());
        i.setAlertaActiva(calcularAlerta(dto.stockDisponible(), dto.stockMinimo()));
        i.setUltimaActualizacion(LocalDateTime.now());
    }

    private Short calcularAlerta(java.math.BigDecimal stockDisponible, java.math.BigDecimal stockMinimo) {
        return stockDisponible.compareTo(stockMinimo) <= 0 ? (short) 1 : (short) 0;
    }
}
